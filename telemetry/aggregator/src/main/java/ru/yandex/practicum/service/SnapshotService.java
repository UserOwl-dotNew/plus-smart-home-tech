package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.config.KafkaClient;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class SnapshotService {
    private static final String TELEMETRY_SNAPSHOTS_V1 = "telemetry.snapshots.v1";

    private final KafkaClient kafkaClient;
    private final Map<String, SensorsSnapshotAvro> snapshots = new ConcurrentHashMap<>();

    /**
     * Обработка события от датчика
     */
    public void processSensorEvent(String topic, int partition, long offset, SensorEventAvro event) {
        log.debug("Обработка события: sensorId={}, hubId={}, topic={}, partition={}, offset={}",
                event.getId(), event.getHubId(), topic, partition, offset);

        updateState(event).ifPresent(snapshot -> {
            sendSnapshotToKafka(snapshot);
            log.info("Снапшот обновлен для хаба: {}", snapshot.getHubId());
        });
    }

    /**
     * Обновление состояния снапшота
     */
    Optional<SensorsSnapshotAvro> updateState(SensorEventAvro event) {
        String hubId = event.getHubId();
        String sensorId = event.getId();

        SensorsSnapshotAvro snapshot = snapshots.get(hubId);

        if (snapshot == null) {
            log.debug("Создаем новый снапшот для хаба: {}", hubId);
            snapshot = createNewSnapshot(event);
            snapshots.put(hubId, snapshot);
            return Optional.of(snapshot);
        }

        Map<String, SensorStateAvro> stateMap = snapshot.getSensorsState();
        SensorStateAvro oldState = stateMap.get(sensorId);

        Instant eventTime = event.getTimestamp();

        if (oldState != null) {
            if (eventTime != null) {
                if (oldState.getTimestamp().isAfter(eventTime)) {
                    log.debug("Игнорируем устаревшее событие для датчика: {}", sensorId);
                    return Optional.empty();
                }
            }

            if (oldState.getData().equals(event.getPayload())) {
                log.debug("Данные не изменились для датчика: {}", sensorId);
                return Optional.empty();
            }
        }

        SensorStateAvro newState = SensorStateAvro.newBuilder()
                .setTimestamp(eventTime)
                .setData(event.getPayload())
                .build();

        Map<String, SensorStateAvro> newStateMap = new ConcurrentHashMap<>(stateMap);
        newStateMap.put(sensorId, newState);

        SensorsSnapshotAvro updateSnapshot = SensorsSnapshotAvro.newBuilder()
                .setHubId(hubId)
                .setTimestamp(eventTime)
                .setSensorsState(newStateMap)
                .build();

        snapshots.put(hubId, updateSnapshot);
        return Optional.of(updateSnapshot);
    }

    /**
     * Создание нового снапшота для хаба
     */
    private SensorsSnapshotAvro createNewSnapshot(SensorEventAvro event) {
        Map<String, SensorStateAvro> stateMap = new ConcurrentHashMap<>();

        SensorStateAvro state = SensorStateAvro.newBuilder()
                .setTimestamp(event.getTimestamp())
                .setData(event.getPayload())
                .build();

        stateMap.put(event.getId(), state);

        return SensorsSnapshotAvro.newBuilder()
                .setHubId(event.getHubId())
                .setTimestamp(event.getTimestamp())
                .setSensorsState(stateMap)
                .build();
    }

    /**
     * Отправка снапшота в Kafka (топик telemetry.snapshots.v1)
     */
    private void sendSnapshotToKafka(SensorsSnapshotAvro snapshot) {
        try {
            ProducerRecord<String, SpecificRecordBase> record =
                    new ProducerRecord<>(TELEMETRY_SNAPSHOTS_V1, snapshot.getHubId(), snapshot);

            kafkaClient.getProducer().send(record, (metadata, exception) -> {
                if (exception != null) {
                    log.error("Ошибка отправки снапшота в Kafka: {}", exception.getMessage(), exception);
                } else {
                    log.info("Снапшот отправлен в Kafka: topic={}, partition={}, offset={}",
                            metadata.topic(), metadata.partition(), metadata.offset());
                }
            });
        } catch (Exception e) {
            log.error("Ошибка при отправке снапшота: {}", e.getMessage(), e);
        }
    }
}
