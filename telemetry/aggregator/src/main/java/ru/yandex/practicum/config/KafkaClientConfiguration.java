package ru.yandex.practicum.config;

import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Properties;

@Component
public class KafkaClientConfiguration {

    @Bean
    @Scope("prototype")
    KafkaClient getClient() {
        return new KafkaClient() {

            private Consumer<String, SpecificRecordBase> consumer;
            private Producer<String, SpecificRecordBase> producer;

            @Override
            public Consumer<String, SpecificRecordBase> getConsumer() {
                if (consumer == null) initConsumer();
                return consumer;
            }

            private void initConsumer() {
                Properties config = new Properties();
                config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
                config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
                config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "ru.yandex.practicum.deserializer.SensorEventDeserializer");
                config.put(ConsumerConfig.GROUP_ID_CONFIG, "aggregator-group");
                config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
                consumer = new KafkaConsumer<>(config);
            }

            @Override
            public Producer<String, SpecificRecordBase> getProducer() {
                if (producer == null) initProducer();
                return producer;
            }

            private void initProducer() {
                Properties config = new Properties();
                config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
                config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
                config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "ru.yandex.practicum.serializer.GeneralAvroSerializer");
                producer = new KafkaProducer<>(config);
            }

            @Override
            public void stop() {
                if (consumer != null) consumer.close();
                if (producer != null) producer.close();
            }
        };
    }
}
