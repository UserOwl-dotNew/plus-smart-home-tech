package ru.yandex.practicum.kafka.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.DeviceAddedEventProto;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.kafka.config.KafkaClient;
import ru.yandex.practicum.kafka.config.KafkaTopics;
import ru.yandex.practicum.kafka.mapper.ProtoToAvroMapper;

@Component
public class DeviceAddedEventHandler extends EventHandler implements HubEventHandler {

    private static final Logger log = LoggerFactory.getLogger(DeviceAddedEventHandler.class);

    public DeviceAddedEventHandler(KafkaClient kafkaClient, ProtoToAvroMapper mapper) {
        super(kafkaClient, mapper);
    }

    @Override
    public HubEventProto.PayloadCase getMessageType() {
        return HubEventProto.PayloadCase.DEVICE_ADDED_EVENT_PROTO;
    }

    @Override
    public void handle(HubEventProto event) {
        DeviceAddedEventProto deviceAdded = event.getDeviceAddedEventProto();
        log.info("Устройство добавлено: hubId={}, deviceId={}, type={}",
                event.getHubId(),
                deviceAdded.getId(),
                deviceAdded.getType());

        kafkaProducerSend(event, KafkaTopics.TELEMETRY_HUBS_V1, event.getHubId());
    }
}