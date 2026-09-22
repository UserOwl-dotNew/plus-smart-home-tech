package ru.yandex.practicum.kafka.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.ClimateSensorProto;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.kafka.config.KafkaClient;
import ru.yandex.practicum.kafka.config.KafkaTopics;
import ru.yandex.practicum.kafka.mapper.ProtoToAvroMapper;

@Component
public class ClimateSensorEventHandler extends EventHandler implements SensorEventHandler {

    private static final Logger log = LoggerFactory.getLogger(ClimateSensorEventHandler.class);

    public ClimateSensorEventHandler(KafkaClient kafkaClient, ProtoToAvroMapper mapper) {
        super(kafkaClient, mapper);
    }

    @Override
    public SensorEventProto.PayloadCase getMessageType() {
        return SensorEventProto.PayloadCase.CLIMATE_SENSOR_PROTO;
    }

    @Override
    public void handle(SensorEventProto event) {
        ClimateSensorProto climateData = event.getClimateSensorProto();
        log.info("Обработка климата: temp={}°C, humidity={}%, CO2={}ppm для датчика {}",
                climateData.getTemperatureC(),
                climateData.getHumidity(),
                climateData.getCo2Level(),
                event.getId());

        kafkaProducerSend(event, KafkaTopics.TELEMETRY_SENSORS_V1, event.getId());
    }
}
