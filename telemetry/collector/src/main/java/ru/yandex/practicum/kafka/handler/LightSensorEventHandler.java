package ru.yandex.practicum.kafka.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.LightSensorProto;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.kafka.config.KafkaClient;
import ru.yandex.practicum.kafka.config.KafkaTopics;
import ru.yandex.practicum.kafka.mapper.ProtoToAvroMapper;

@Component
public class LightSensorEventHandler extends EventHandler implements SensorEventHandler {

    private static final Logger log = LoggerFactory.getLogger(LightSensorEventHandler.class);

    public LightSensorEventHandler(KafkaClient kafkaClient, ProtoToAvroMapper mapper) {
        super(kafkaClient, mapper);
    }

    @Override
    public SensorEventProto.PayloadCase getMessageType() {
        return SensorEventProto.PayloadCase.LIGHT_SENSOR_PROTO;
    }

    @Override
    public void handle(SensorEventProto event) {
        LightSensorProto lightData = event.getLightSensorProto();
        log.info("Обработка освещенности: {} люкс для датчика {}",
                lightData.getLuminosity(),
                event.getId());

        kafkaProducerSend(event, KafkaTopics.TELEMETRY_SENSORS_V1, event.getId());
    }
}
