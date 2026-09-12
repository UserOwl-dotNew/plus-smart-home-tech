package ru.yandex.practicum.starter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.config.KafkaClient;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.service.SnapshotService;

import java.time.Duration;
import java.util.List;

/**
 * Класс AggregationStarter, ответственный за запуск агрегации данных.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AggregationStarter {

    private static final Duration CONSUME_ATTEMPT_TIMEOUT = Duration.ofMillis(1000);
    private static final List<String> TOPICS = List.of("telemetry.sensors.v1");

    private final KafkaClient kafkaClient;
    private final SnapshotService snapshotService;

    /**
     * Метод для начала процесса агрегации данных.
     * Подписывается на топики для получения событий от датчиков,
     * формирует снимок их состояния и записывает в кафку.
     */
    public void start() {

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Получен сигнал завершения. Останавливаем consumer...");
            kafkaClient.getConsumer().wakeup();
        }));

        try {
            kafkaClient.getConsumer().subscribe(TOPICS);
            log.info("Aggregator подписан на топик: {}", TOPICS);

            while (true) {
                ConsumerRecords<String, SpecificRecordBase> records =
                        kafkaClient.getConsumer().poll(CONSUME_ATTEMPT_TIMEOUT);

                int count = 0;
                for (ConsumerRecord<String, SpecificRecordBase> record : records) {
                    SensorEventAvro event = (SensorEventAvro) record.value();
                    snapshotService.processSensorEvent(record.topic(), record.partition(), record.offset(), event);
                }

                kafkaClient.getConsumer().commitAsync((offsets, exception) -> {
                    if (exception != null) log.warn("Ошибка во время фиксации оффсетов: {}", offsets, exception);
                });
            }

        } catch (WakeupException ignored) {
            log.info("Consumer разбужен для завершения");
        } catch (Exception e) {
            log.error("Ошибка во время обработки событий от датчиков", e);
        } finally {

            try {
                // Перед тем, как закрыть продюсер и консьюмер, нужно убедиться,
                // что все сообщения, лежащие в буффере, отправлены и
                // все оффсеты обработанных сообщений зафиксированы

                // здесь нужно вызвать метод продюсера для сброса данных в буффере
                // здесь нужно вызвать метод консьюмера для фиксации смещений
                log.info("Выполняем синхронный коммит и сбрасываем буферы...");
                kafkaClient.getConsumer().commitSync();
                kafkaClient.getProducer().flush();

            } finally {
                log.info("Закрываем консьюмер");
                kafkaClient.getConsumer().close();
                log.info("Закрываем продюсер");
                kafkaClient.getProducer().close();
            }
        }
    }
}