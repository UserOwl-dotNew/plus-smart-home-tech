package ru.yandex.practicum.analyzer.config;

import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Properties;

@Component
public class KafkaConsumerConfiguration {

    @Bean
    @Scope("prototype")
    KafkaConsumerClient getConsumer() {
        return new KafkaConsumerClient() {

            private Consumer<String, SpecificRecordBase> snapshotConsumer;
            private Consumer<String, SpecificRecordBase> hubEventConsumer;

            @Override
            public Consumer<String, SpecificRecordBase> getSnapshotConsumer() {
                if (snapshotConsumer == null) initSnapshotConsumer();
                return snapshotConsumer;
            }

            private void initSnapshotConsumer() {
                Properties config = new Properties();
                config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
                config.put(ConsumerConfig.GROUP_ID_CONFIG, "analyzer-snapshot-group");
                config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
                config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "ru.yandex.practicum.analyzer.deserializer.SensorsSnapshotAvroDeserializer");
                config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
                config.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 100);
                snapshotConsumer = new KafkaConsumer<>(config);
            }

            @Override
            public Consumer<String, SpecificRecordBase> getHubEventConsumer() {
                if (hubEventConsumer == null) initHubEventConsumer();
                return hubEventConsumer;
            }

            private void initHubEventConsumer() {
                Properties config = new Properties();
                config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
                config.put(ConsumerConfig.GROUP_ID_CONFIG, "analyzer-hub-event-group");
                config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
                config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "ru.yandex.practicum.analyzer.deserializer.HubEventAvroDeserializer");
                config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
                config.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 50);
                hubEventConsumer = new KafkaConsumer<>(config);
            }

            @Override
            public void stop() {
                if (snapshotConsumer != null) snapshotConsumer.close();
                if (hubEventConsumer != null) hubEventConsumer.close();
            }
        };
    }
}
