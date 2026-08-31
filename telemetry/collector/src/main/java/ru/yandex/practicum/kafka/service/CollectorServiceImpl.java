package ru.yandex.practicum.kafka.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.kafka.config.KafkaClient;
import ru.yandex.practicum.kafka.config.KafkaTopics;
import ru.yandex.practicum.kafka.event.*;
import ru.yandex.practicum.kafka.mapper.EventMapper;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CollectorServiceImpl implements CollectorService {
    private final KafkaClient kafkaClient;
    private final EventMapper mapper;

    @Override
    public void processHubEvent(HubEvent event) {
        SpecificRecordBase avroEvent = mapToAvro(event);
        sendToKafka(KafkaTopics.TELEMETRY_HUBS_V1, event.getHubId(), avroEvent);
    }

    @Override
    public void processSensorEvent(SensorEvent event) {
        SpecificRecordBase avroEvent = mapToAvro(event);
        sendToKafka(KafkaTopics.TELEMETRY_SENSORS_V1, event.getId(), avroEvent);
    }

    private SpecificRecordBase mapToAvro(SensorEvent event) {
        return switch (event) {
            case LightSensorEvent e -> mapper.toSensorEventAvro(e);
            case TemperatureSensorEvent e -> mapper.toSensorEventAvro(e);
            case ClimateSensorEvent e -> mapper.toSensorEventAvro(e);
            case MotionSensorEvent e -> mapper.toSensorEventAvro(e);
            case SwitchSensorEvent e -> mapper.toSensorEventAvro(e);
            default -> throw new IllegalArgumentException("Unknown sensor event type: " + event.getClass());
        };
    }

    private SpecificRecordBase mapToAvro(HubEvent event) {
        return switch (event) {
            case DeviceAddedEvent e -> mapper.toHubEventAvro(e);
            case DeviceRemovedEvent e -> mapper.toHubEventAvro(e);
            case ScenarioAddedEvent e -> mapper.toHubEventAvro(e);
            case ScenarioRemovedEvent e -> mapper.toHubEventAvro(e);
            default -> throw new IllegalArgumentException("Unknown hub event type: " + event.getClass());
        };
    }

    private void sendToKafka(String topic, String key, SpecificRecordBase value) {
        try {
            log.info("Sending to Kafka - topic: {}, key: {}, class: {}, schema: {}",
                    topic, key, value.getClass().getSimpleName(), value.getSchema());

            ProducerRecord<String, SpecificRecordBase> record =
                    new ProducerRecord<>(topic, key, value);
            kafkaClient.getProducer().send(record);
        } catch (Exception e) {
            log.error("Failed to send to Kafka", e);
            throw new RuntimeException("Failed to send to Kafka", e);
        }
    }
}
