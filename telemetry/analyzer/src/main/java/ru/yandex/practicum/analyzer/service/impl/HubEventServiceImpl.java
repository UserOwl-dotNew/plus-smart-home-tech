package ru.yandex.practicum.analyzer.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.analyzer.entity.*;
import ru.yandex.practicum.analyzer.mapper.AvroToEntityMapper;
import ru.yandex.practicum.analyzer.repository.ActionRepository;
import ru.yandex.practicum.analyzer.repository.ConditionRepository;
import ru.yandex.practicum.analyzer.repository.ScenarioRepository;
import ru.yandex.practicum.analyzer.repository.SensorRepository;
import ru.yandex.practicum.analyzer.service.HubEventService;
import ru.yandex.practicum.kafka.telemetry.event.*;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class HubEventServiceImpl implements HubEventService {

    private final AvroToEntityMapper mapper;
    private final ActionRepository actionRepository;
    private final ConditionRepository conditionRepository;
    private final ScenarioRepository scenarioRepository;
    private final SensorRepository sensorRepository;

    @Override
    public void handleDeviceAdded(DeviceAddedEventAvro event, String hubId) {
        if (sensorRepository.existsByIdInAndHubId(List.of(event.getId()), hubId)) {
            log.debug("Sensor with id: {} and hubId: {} already exists, skipping", event.getId(), hubId);
            return;
        }

        Sensor sensor = Sensor.builder()
                .id(event.getId())
                .hubId(hubId)
                .build();

        sensorRepository.save(sensor);
    }

    @Override
    public void handleDeviceRemoved(DeviceRemovedEventAvro event, String hubId) {
        Optional<Sensor> sensorOpt = sensorRepository.findByIdAndHubId(event.getId(), hubId);

        if (sensorOpt.isEmpty()) {
            log.debug("Sensor with id: {} and hubId: {} not found, skipping", event.getId(), hubId);
            return;
        }

        Sensor sensor = sensorOpt.get();

        List<Scenario> scenarios = scenarioRepository.findByHubId(hubId);

        for (Scenario scenario : scenarios) {
            scenario.getConditions().removeIf(sc ->
                    sc.getSensor().getId().equals(event.getId()));

            scenario.getActions().removeIf(sc ->
                    sc.getSensor().getId().equals(event.getId()));

            scenarioRepository.save(scenario);
        }
        sensorRepository.delete(sensor);
    }

    @Override
    public void handleScenarioAdded(ScenarioAddedEventAvro event, String hubId) {
        Optional<Scenario> existingOpt = scenarioRepository.findByHubIdAndName(hubId, event.getName());

        if (existingOpt.isPresent()) {
            log.debug("Scenario with name: {} and hubId: {} already exists, skipping", event.getName(), hubId);
            return;
        }

        Scenario scenario = Scenario.builder()
                .hubId(hubId)
                .name(event.getName())
                .build();

        scenario = scenarioRepository.save(scenario);

        for (ScenarioConditionAvro conditionAvro : event.getConditions()) {
            Condition condition = mapper.toCondition(conditionAvro);
            condition = conditionRepository.save(condition);

            Sensor sensor = sensorRepository.findByIdAndHubId(conditionAvro.getSensorId(), hubId)
                    .orElseGet(() -> {
                        Sensor newSensor = Sensor.builder()
                                .id(conditionAvro.getSensorId())
                                .hubId(hubId)
                                .build();
                        return sensorRepository.save(newSensor);
                    });

            ScenarioCondition scenarioCondition = ScenarioCondition.builder()
                    .scenario(scenario)
                    .condition(condition)
                    .sensor(sensor)
                    .build();

            scenario.getConditions().add(scenarioCondition);
        }

        for (DeviceActionAvro actionAvro : event.getActions()) {
            Action action = mapper.toAction(actionAvro);
            action = actionRepository.save(action);

            Sensor sensor = sensorRepository.findByIdAndHubId(actionAvro.getSensorId(), hubId)
                    .orElseGet(() -> {
                        Sensor newSensor = Sensor.builder()
                                .id(actionAvro.getSensorId())
                                .hubId(hubId)
                                .build();
                        return sensorRepository.save(newSensor);
                    });

            ScenarioAction scenarioAction = ScenarioAction.builder()
                    .scenario(scenario)
                    .action(action)
                    .sensor(sensor)
                    .build();

            scenario.getActions().add(scenarioAction);
        }

        scenarioRepository.save(scenario);
    }

    @Override
    public void handleScenarioRemoved(ScenarioRemovedEventAvro event, String hubId) {
        Optional<Scenario> scenarioOpt = scenarioRepository.findByHubIdAndName(hubId, event.getName());

        if (scenarioOpt.isEmpty()) {
            log.debug("Scenario with name: {} and hubId: {} not found, skipping", event.getName(), hubId);
            return;
        }

        scenarioRepository.delete(scenarioOpt.get());
    }
}
