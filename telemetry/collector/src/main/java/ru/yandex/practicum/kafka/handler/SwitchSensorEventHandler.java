package ru.yandex.practicum.kafka.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.grpc.telemetry.event.SwitchSensorProto;
import ru.yandex.practicum.kafka.config.KafkaClient;
import ru.yandex.practicum.kafka.config.KafkaTopics;
import ru.yandex.practicum.kafka.mapper.ProtoToAvroMapper;

@Component
public class SwitchSensorEventHandler extends EventHandler implements SensorEventHandler {

    private static final Logger log = LoggerFactory.getLogger(SwitchSensorEventHandler.class);

    public SwitchSensorEventHandler(KafkaClient kafkaClient, ProtoToAvroMapper mapper) {
        super(kafkaClient, mapper);
    }

    @Override
    public SensorEventProto.PayloadCase getMessageType() {
        return SensorEventProto.PayloadCase.SWITCH_SENSOR_PROTO;
    }

    @Override
    public void handle(SensorEventProto event) {
        SwitchSensorProto switchData = event.getSwitchSensorProto();
        log.info("Обработка переключателя: состояние={} для датчика {}",
                switchData.getState() ? "ВКЛ" : "ВЫКЛ",
                event.getId());

        kafkaProducerSend(event, KafkaTopics.TELEMETRY_SENSORS_V1, event.getId());
    }
}
