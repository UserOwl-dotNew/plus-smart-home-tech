package ru.yandex.practicum.kafka.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.grpc.telemetry.event.ScenarioRemovedEventProto;
import ru.yandex.practicum.kafka.config.KafkaClient;
import ru.yandex.practicum.kafka.config.KafkaTopics;
import ru.yandex.practicum.kafka.mapper.ProtoToAvroMapper;

@Component
public class ScenarioRemovedEventHandler extends EventHandler implements HubEventHandler {

    private static final Logger log = LoggerFactory.getLogger(ScenarioRemovedEventHandler.class);

    public ScenarioRemovedEventHandler(KafkaClient kafkaClient, ProtoToAvroMapper mapper) {
        super(kafkaClient, mapper);
    }

    @Override
    public HubEventProto.PayloadCase getMessageType() {
        return HubEventProto.PayloadCase.SCENARIO_REMOVED_EVENT_PROTO;
    }

    @Override
    public void handle(HubEventProto event) {
        ScenarioRemovedEventProto scenarioRemoved = event.getScenarioRemovedEventProto();
        log.info("Сценарий удален: hubId={}, scenarioId={}",
                event.getHubId(),
                scenarioRemoved.getName());

        kafkaProducerSend(event, KafkaTopics.TELEMETRY_HUBS_V1, event.getHubId());
    }
}