package ru.yandex.practicum.kafka.handler;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.kafka.config.KafkaClient;
import ru.yandex.practicum.kafka.mapper.ProtoToAvroMapper;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;

public abstract class EventHandler {
    private Logger log = LoggerFactory.getLogger(getClass());
    private final ProtoToAvroMapper mapper;
    private final KafkaClient kafkaClient;

    public EventHandler(KafkaClient kafkaClient, ProtoToAvroMapper mapper) {
        this.kafkaClient = kafkaClient;
        this.mapper = mapper;
    }

    void kafkaProducerSend(HubEventProto event, String topics, String id) {
        try {
            HubEventAvro avroEvent = mapper.mapToHubEventAvro(event);

            kafkaClient.getProducer().send(new ProducerRecord<>(
                    topics,
                    id,
                    avroEvent
            ), (metadata, exception) -> {
                if (exception != null) {
                    log.error("Ошибка отправки в Kafka: {}", exception.getMessage(), exception);
                } else {
                    log.info("Успешно отправлено в Kafka: topic={}, partition={}, offset={}",
                            metadata.topic(), metadata.partition(), metadata.offset());
                }
            });
        } catch (Exception e) {
            log.error("Ошибка при обработке события: {}", e.getMessage(), e);
        }
    }

    void kafkaProducerSend(SensorEventProto event, String topics, String id) {
        try {
            SensorEventAvro avroEvent = mapper.mapToSensorEventAvro(event);

            kafkaClient.getProducer().send(new ProducerRecord<>(
                    topics,
                    id,
                    avroEvent
            ), (metadata, exception) -> {
                if (exception != null) {
                    log.error("Ошибка отправки в Kafka: {}", exception.getMessage(), exception);
                } else {
                    log.info("Успешно отправлено в Kafka: topic={}, partition={}, offset={}",
                            metadata.topic(), metadata.partition(), metadata.offset());
                }
            });
        } catch (Exception e) {
            log.error("Ошибка при обработке события: {}", e.getMessage(), e);
        }
    }
}
