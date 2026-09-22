package ru.yandex.practicum.kafka.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.grpc.telemetry.event.TemperatureSensorProto;
import ru.yandex.practicum.kafka.config.KafkaClient;
import ru.yandex.practicum.kafka.config.KafkaTopics;
import ru.yandex.practicum.kafka.mapper.ProtoToAvroMapper;

@Component
public class TemperatureSensorEventHandler extends EventHandler implements SensorEventHandler {

    private static final Logger log = LoggerFactory.getLogger(TemperatureSensorEventHandler.class);

    public TemperatureSensorEventHandler(KafkaClient kafkaClient, ProtoToAvroMapper mapper) {
        super(kafkaClient, mapper);
    }

    @Override
    public SensorEventProto.PayloadCase getMessageType() {
        return SensorEventProto.PayloadCase.TEMPERATURE_SENSOR_PROTO;
    }

    @Override
    public void handle(SensorEventProto event) {
        TemperatureSensorProto tempData = event.getTemperatureSensorProto();
        log.info("Обработка температуры: {}°C ({}°F) для датчика {}",
                tempData.getTemperatureC(),
                tempData.getTemperatureF(),
                event.getId());

        kafkaProducerSend(event, KafkaTopics.TELEMETRY_SENSORS_V1, event.getId());
    }
}
