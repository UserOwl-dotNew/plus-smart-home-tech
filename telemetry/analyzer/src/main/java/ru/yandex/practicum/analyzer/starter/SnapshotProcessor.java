package ru.yandex.practicum.analyzer.starter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.analyzer.config.KafkaConsumerClient;
import ru.yandex.practicum.analyzer.service.ScenarioService;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.time.Duration;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SnapshotProcessor implements Runnable {

    private static final List<String> TOPIC = List.of("telemetry.snapshots.v1");
    private static final Duration CONSUME_ATTEMPT_TIMEOUT = Duration.ofMillis(1000);

    private final KafkaConsumerClient kafkaClient;
    private final ScenarioService scenarioService;

    @Override
    public void run() {

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Получен сигнал завершения. Останавливаем snapshotConsumer...");
            kafkaClient.getSnapshotConsumer().wakeup();
        }));

        try {
            kafkaClient.getSnapshotConsumer().subscribe(TOPIC);
            log.info("SnapshotConsumer подписан на топик: {}", TOPIC);

            while (true) {
                ConsumerRecords<String, SpecificRecordBase> records =
                        kafkaClient.getSnapshotConsumer().poll(CONSUME_ATTEMPT_TIMEOUT);

                for (ConsumerRecord<String, SpecificRecordBase> record : records) {
                    SensorsSnapshotAvro snapshotAvro = (SensorsSnapshotAvro) record.value();
                    scenarioService.processSnapshot(snapshotAvro);
                }

                kafkaClient.getSnapshotConsumer()
                        .commitAsync((offsets, exception) -> {
                            if (exception != null)
                                log.warn("Ошибка во время фиксации оффсетов: {}", offsets, exception);
                        });
            }
        } catch (WakeupException ignored) {
            log.info("SnapshotConsumer разбужен для завершения");
        } catch (Exception e) {
            log.error("Ошибка во время обработки событий от датчиков", e);
        } finally {
            try {
                log.info("Выполняем синхронный коммит...");
                kafkaClient.getSnapshotConsumer().commitSync();
            } finally {
                log.info("Закрываем консьюмер");
                kafkaClient.getSnapshotConsumer().close();
            }
        }

    }
}
