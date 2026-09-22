package ru.yandex.practicum.kafka.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.DeviceRemovedEventProto;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.kafka.config.KafkaClient;
import ru.yandex.practicum.kafka.config.KafkaTopics;
import ru.yandex.practicum.kafka.mapper.ProtoToAvroMapper;

@Component
public class DeviceRemovedEventHandler extends EventHandler implements HubEventHandler {

    private static final Logger log = LoggerFactory.getLogger(DeviceRemovedEventHandler.class);

    public DeviceRemovedEventHandler(KafkaClient kafkaClient, ProtoToAvroMapper mapper) {
        super(kafkaClient, mapper);
    }

    @Override
    public HubEventProto.PayloadCase getMessageType() {
        return HubEventProto.PayloadCase.DEVICE_REMOVED_EVENT_PROTO;
    }

    @Override
    public void handle(HubEventProto event) {
        DeviceRemovedEventProto deviceRemoved = event.getDeviceRemovedEventProto();
        log.info("Устройство удалено: hubId={}, deviceId={}",
                event.getHubId(),
                deviceRemoved.getId());

        kafkaProducerSend(event, KafkaTopics.TELEMETRY_HUBS_V1, event.getHubId());
    }
}