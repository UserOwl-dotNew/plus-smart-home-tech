package ru.yandex.practicum.analyzer.config;


import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.Consumer;

public interface KafkaConsumerClient {
    Consumer<String, SpecificRecordBase> getSnapshotConsumer();

    Consumer<String, SpecificRecordBase> getHubEventConsumer();

    void stop();
}
