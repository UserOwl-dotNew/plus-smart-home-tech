package ru.yandex.practicum.analyzer.service.impl;

import com.google.protobuf.Empty;
import com.google.protobuf.Timestamp;
import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.analyzer.entity.Scenario;
import ru.yandex.practicum.analyzer.entity.ScenarioAction;
import ru.yandex.practicum.analyzer.entity.ScenarioCondition;
import ru.yandex.practicum.analyzer.enums.ActionType;
import ru.yandex.practicum.analyzer.enums.ConditionType;
import ru.yandex.practicum.analyzer.repository.ScenarioRepository;
import ru.yandex.practicum.analyzer.service.ScenarioService;
import ru.yandex.practicum.grpc.telemetry.event.ActionTypeProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionRequest;
import ru.yandex.practicum.grpc.telemetry.hubrouter.HubRouterControllerGrpc;
import ru.yandex.practicum.kafka.telemetry.event.*;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScenarioServiceImpl implements ScenarioService {

    private final HubRouterControllerGrpc.HubRouterControllerBlockingStub hubRouterClient;
    private final ScenarioRepository scenarioRepository;

    @Override
    public void processSnapshot(SensorsSnapshotAvro snapshot) {
        String hubId = snapshot.getHubId();

        List<Scenario> scenarios = scenarioRepository.findByHubId(hubId);

        if (scenarios.isEmpty()) {
            log.debug("Нет сценариев для хаба: {}", hubId);
            return;
        }

        for (Scenario scenario : scenarios) {
            if (checkScenario(scenario, snapshot)) {
                executeActions(scenario, snapshot);
            }
        }
    }

    @Override
    public boolean checkScenario(Scenario scenario, SensorsSnapshotAvro snapshot) {
        for (ScenarioCondition condition : scenario.getConditions()) {
            SensorStateAvro sensorState = snapshot.getSensorsState().
                    get(condition.getSensor().getId());

            if (sensorState == null) {
                log.warn("Датчик {} не найден в снапшоте", condition.getSensor().getId());
                return false;
            }

            if (!isConditionMet(sensorState, condition)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void executeActions(Scenario scenario, SensorsSnapshotAvro snapshot) {
        List<ScenarioAction> actions = scenario.getActions();

        if (actions.isEmpty()) {
            log.debug("Нет действий для сценария: {}", scenario.getName());
            return;
        }

        log.info("Выполняем {} действий для сценария: {}", actions.size(), scenario.getName());

        for (ScenarioAction action : actions) {
            sendGrpcCommand(scenario, action, snapshot);
        }
    }

    /**
     * Проверка одного условия
     */
    private boolean isConditionMet(SensorStateAvro sensorState, ScenarioCondition condition) {
        int actualValue = getSensorValue(sensorState, condition.getCondition().getType());
        int expectedValue = condition.getCondition().getValue();

        return switch (condition.getCondition().getOperation()) {
            case EQUALS -> actualValue == expectedValue;
            case GREATER_THAN -> actualValue > expectedValue;
            case LOWER_THAN -> actualValue < expectedValue;
        };
    }

    private ActionTypeProto mapActionType(ActionType type) {
        return switch (type) {
            case ACTIVATE -> ActionTypeProto.ACTIVATE;
            case DEACTIVATE -> ActionTypeProto.DEACTIVATE;
            case INVERSE -> ActionTypeProto.INVERSE;
            case SET_VALUE -> ActionTypeProto.SET_VALUE;
        };
    }

    /**
     * Получить значение из снапшота
     */
    private int getSensorValue(SensorStateAvro state, ConditionType type) {
        Object data = state.getData();

        if (data instanceof TemperatureSensorEventAvro temp) {
            return temp.getTemperatureC();
        }

        if (data instanceof ClimateSensorEventAvro climate) {
            return switch (type) {
                case TEMPERATURE -> climate.getTemperatureC();
                case HUMIDITY -> climate.getHumidity();
                case CO2LEVEL -> climate.getHumidity();
                default -> throw new IllegalArgumentException("Unsupported: " + type);
            };
        }

        if (data instanceof LightSensorEventAvro light) {
            return light.getLuminosity();
        }

        if (data instanceof MotionSensorEventAvro motion) {
            return motion.getMotion() ? 1 : 0;
        }

        if (data instanceof SwitchSensorEventAvro switchSensor) {
            return switchSensor.getState() ? 1 : 0;
        }

        throw new IllegalArgumentException("Unknown condition type: " + type);
    }

    /**
     * Отправка команды через gRPC
     */
    private void sendGrpcCommand(Scenario scenario, ScenarioAction action, SensorsSnapshotAvro snapshot) {
        try {
            DeviceActionProto actionProto = DeviceActionProto.newBuilder()
                    .setSensorId(action.getSensor().getId())
                    .setType(mapActionType(action.getAction().getType()))
                    .setValue(action.getAction().getValue() != null ? action.getAction().getValue() : 0)
                    .build();

            DeviceActionRequest request = DeviceActionRequest.newBuilder()
                    .setHubId(snapshot.getHubId())
                    .setScenarioName(scenario.getName())
                    .setAction(actionProto)
                    .setTimestamp(Timestamp.newBuilder()
                            .setSeconds(Instant.now().getEpochSecond())
                            .setNanos(Instant.now().getNano()))
                    .build();

            Empty response = hubRouterClient.handleDeviceAction(request);

            log.info("Команда отправлена: scenario='{}', action={}, device={}",
                    scenario.getName(),
                    action.getAction().getType(),
                    action.getSensor().getId());
        } catch (StatusRuntimeException e) {
            log.error("gRPC ошибка при отправке команды: status={}, message={}",
                    e.getStatus().getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("Ошибка при отправке команды: {}", e.getMessage(), e);
        }
    }
}
