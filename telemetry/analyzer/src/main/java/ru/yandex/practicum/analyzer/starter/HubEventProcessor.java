package ru.yandex.practicum.analyzer.starter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.analyzer.config.KafkaConsumerClient;
import ru.yandex.practicum.analyzer.enums.HubEventType;
import ru.yandex.practicum.analyzer.service.HubEventService;
import ru.yandex.practicum.kafka.telemetry.event.*;

import java.time.Duration;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class HubEventProcessor implements Runnable {

    private static final List<String> TOPIC = List.of("telemetry.hubs.v1");
    private static final Duration CONSUME_ATTEMPT_TIMEOUT = Duration.ofMillis(1000);

    private final KafkaConsumerClient kafkaClient;
    private final HubEventService hubEventService;

    @Override
    public void run() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Получен сигнал завершения. Останавливаем hubEventConsumer...");
            kafkaClient.getHubEventConsumer().wakeup();
        }));

        try {
            kafkaClient.getHubEventConsumer().subscribe(TOPIC);
            log.info("HubEventConsumer подписан на топик: {}", TOPIC);

            while (true) {
                ConsumerRecords<String, SpecificRecordBase> records =
                        kafkaClient.getHubEventConsumer().poll(CONSUME_ATTEMPT_TIMEOUT);

                for (ConsumerRecord<String, SpecificRecordBase> record : records) {
                    HubEventAvro hubEventProcessor = (HubEventAvro) record.value();
                    String hubId = hubEventProcessor.getHubId();
                    HubEventType eventType = mapToHubEvent(hubEventProcessor.getPayload());

                    try {
                        completeHandle(eventType, (SpecificRecordBase) hubEventProcessor.getPayload(), hubId);
                    } catch (Exception e) {
                        log.error("Ошибка при обработке события {}: {}", eventType, e.getMessage(), e);
                    }
                }

                kafkaClient.getHubEventConsumer()
                        .commitAsync((offsets, exception) -> {
                            if (exception != null)
                                log.warn("Ошибка во время фиксации оффсетов: {}", offsets, exception);
                        });
            }
        } catch (WakeupException e) {
            log.info("SnapshotConsumer разбужен для завершения");
        } catch (Exception e) {
            log.error("Ошибка во время обработки событий от датчиков", e);
        } finally {
            try {
                log.info("Выполняем синхронный коммит...");
                kafkaClient.getHubEventConsumer().commitSync();
            } finally {
                log.info("Закрываем консьюмер");
                kafkaClient.getHubEventConsumer().close();
            }
        }
    }

    private HubEventType mapToHubEvent(Object payload) {
        if (payload == null) {
            throw new IllegalArgumentException("Payload is null");
        }
        String schemaName = ((SpecificRecordBase) payload).getSchema().getName();
        return switch (schemaName) {
            case "DeviceAddedEventAvro" -> HubEventType.DEVICE_ADDED;
            case "DeviceRemovedEventAvro" -> HubEventType.DEVICE_REMOVED;
            case "ScenarioAddedEventAvro" -> HubEventType.SCENARIO_ADDED;
            case "ScenarioRemovedEventAvro" -> HubEventType.SCENARIO_REMOVED;
            default -> throw new IllegalArgumentException("Unknown payload type: " + payload.getClass());
        };
    }

    private void completeHandle(HubEventType eventType, SpecificRecordBase event, String hubId) {
        switch (eventType) {
            case DEVICE_ADDED -> hubEventService.handleDeviceAdded((DeviceAddedEventAvro) event, hubId);
            case DEVICE_REMOVED -> hubEventService.handleDeviceRemoved((DeviceRemovedEventAvro) event, hubId);
            case SCENARIO_ADDED -> hubEventService.handleScenarioAdded((ScenarioAddedEventAvro) event, hubId);
            case SCENARIO_REMOVED -> hubEventService.handleScenarioRemoved((ScenarioRemovedEventAvro) event, hubId);
        }
    }
}
