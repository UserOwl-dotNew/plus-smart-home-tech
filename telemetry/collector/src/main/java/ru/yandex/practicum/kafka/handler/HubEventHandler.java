package ru.yandex.practicum.kafka.handler;

import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;

public interface HubEventHandler {
    HubEventProto.PayloadCase getMessageType();

    void handle(HubEventProto event);
}