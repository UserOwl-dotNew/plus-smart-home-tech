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

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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
        }
        scenarioRepository.saveAll(scenarios);
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

        /**
         * Собираем все sensorIds
         */
        Set<String> sensorIds = new HashSet<>();
        event.getConditions().forEach(c -> sensorIds.add(c.getSensorId()));
        event.getActions().forEach(a -> sensorIds.add(a.getSensorId()));

        /**
         * Одним запросом находим все существующие сенсоры
         */
        List<Sensor> existingSensors = sensorRepository.findAllById(sensorIds);
        Map<String, Sensor> sensorMap = existingSensors.stream()
                .collect(Collectors.toMap(Sensor::getId, Function.identity()));

        /**
         * Создаем отсутствующие сенсоры
         */
        List<Sensor> newSensors = sensorIds.stream()
                .filter(id -> !sensorMap.containsKey(id))
                .map(id -> Sensor.builder().hubId(hubId).id(id).build())
                .toList();
        List<Sensor> savedSensors = sensorRepository.saveAll(newSensors);
        savedSensors.forEach(s -> sensorMap.put(s.getId(), s));


        /**
         * Находим все условия
         */
        List<ScenarioConditionAvro> conditionAvros = event.getConditions();

        /**
         * Преобразуем все условия в entity и сохраняем в бд
         */
        List<Condition> conditions = conditionRepository.saveAll(conditionAvros.stream()
                .map(mapper::toCondition)
                .toList());

        /**
         * Находим все действия
         */
        List<DeviceActionAvro> actionAvros = event.getActions();

        /**
         * Преобразуем все действия в entity и сохраняем в бд
         */
        List<Action> actions = actionRepository.saveAll(actionAvros.stream()
                .map(mapper::toAction)
                .toList());

        for (int i = 0; i < conditionAvros.size(); i++) {
            ScenarioConditionAvro conditionAvro = conditionAvros.get(i);
            Condition condition = conditions.get(i);
            Sensor sensor = sensorMap.get(conditionAvro.getSensorId());


            ScenarioCondition scenarioCondition = ScenarioCondition.builder()
                    .scenario(scenario)
                    .condition(condition)
                    .sensor(sensor)
                    .build();

            scenario.getConditions().add(scenarioCondition);
        }

        for (int i = 0; i < actionAvros.size(); i++) {
            DeviceActionAvro actionAvro = actionAvros.get(i);
            Action action = actions.get(i);
            Sensor sensor = sensorMap.get(actionAvro.getSensorId());

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
