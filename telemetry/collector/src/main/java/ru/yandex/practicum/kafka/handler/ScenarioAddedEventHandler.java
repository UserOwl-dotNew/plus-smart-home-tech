package ru.yandex.practicum.kafka.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.grpc.telemetry.event.ScenarioAddedEventProto;
import ru.yandex.practicum.kafka.config.KafkaClient;
import ru.yandex.practicum.kafka.config.KafkaTopics;
import ru.yandex.practicum.kafka.mapper.ProtoToAvroMapper;

@Component
public class ScenarioAddedEventHandler extends EventHandler implements HubEventHandler {

    private static final Logger log = LoggerFactory.getLogger(ScenarioAddedEventHandler.class);

    public ScenarioAddedEventHandler(KafkaClient kafkaClient, ProtoToAvroMapper mapper) {
        super(kafkaClient, mapper);
    }

    @Override
    public HubEventProto.PayloadCase getMessageType() {
        return HubEventProto.PayloadCase.SCENARIO_ADDED_EVENT_PROTO;
    }

    @Override
    public void handle(HubEventProto event) {
        ScenarioAddedEventProto scenarioAdded = event.getScenarioAddedEventProto();
        log.info("Сценарий добавлен: hubId={}, name={}",
                event.getHubId(),
                scenarioAdded.getName());

        kafkaProducerSend(event, KafkaTopics.TELEMETRY_HUBS_V1, event.getHubId());
    }
}