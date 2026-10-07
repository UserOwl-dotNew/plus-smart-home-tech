package ru.yandex.practicum.kafka.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.MotionSensorProto;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.kafka.config.KafkaClient;
import ru.yandex.practicum.kafka.config.KafkaTopics;
import ru.yandex.practicum.kafka.mapper.ProtoToAvroMapper;

@Component
public class MotionSensorEventHandler extends EventHandler implements SensorEventHandler {

    private static final Logger log = LoggerFactory.getLogger(MotionSensorEventHandler.class);

    public MotionSensorEventHandler(KafkaClient kafkaClient, ProtoToAvroMapper mapper) {
        super(kafkaClient, mapper);
    }

    @Override
    public SensorEventProto.PayloadCase getMessageType() {
        return SensorEventProto.PayloadCase.MOTION_SENSOR_PROTO;
    }

    @Override
    public void handle(SensorEventProto event) {
        MotionSensorProto motionData = event.getMotionSensorProto();
        log.info("Обработка движения: linkQuality={}, voltage={} для датчика {}",
                motionData.getLinkQuality(),
                motionData.getVoltage(),
                event.getId());

        kafkaProducerSend(event, KafkaTopics.TELEMETRY_SENSORS_V1, event.getId());
    }
}
