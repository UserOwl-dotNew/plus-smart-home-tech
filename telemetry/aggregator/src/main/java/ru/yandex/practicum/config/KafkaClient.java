package ru.yandex.practicum.config;

import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.producer.Producer;

public interface KafkaClient {
    Consumer<String, SpecificRecordBase> getConsumer();

    Producer<String, SpecificRecordBase> getProducer();

    void stop();
}
