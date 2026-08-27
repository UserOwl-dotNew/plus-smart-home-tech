package ru.yandex.practicum.kafka.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.yandex.practicum.kafka.enums.ActionType;
import ru.yandex.practicum.kafka.enums.ConditionType;
import ru.yandex.practicum.kafka.enums.DeviceType;
import ru.yandex.practicum.kafka.enums.OperationType;
import ru.yandex.practicum.kafka.event.*;
import ru.yandex.practicum.kafka.telemetry.event.*;

/**
 * Маппер для событий
 */
@Mapper(componentModel = "spring")
public interface EventMapper {

    // Датчики
    ClimateSensorEventAvro toAvro(ClimateSensorEvent event);

    @Mapping(source = "linkQuality", target = "linkQuality")
    LightSensorEventAvro toAvro(LightSensorEvent event);

    @Mapping(source = "linkQuality", target = "linkQuality")
    MotionSensorEventAvro toAvro(MotionSensorEvent event);

    @Mapping(source = "temperatureC", target = "temperatureC")
    @Mapping(source = "temperatureF", target = "temperatureF")
    TemperatureSensorEventAvro toAvro(TemperatureSensorEvent event);

    SwitchSensorEventAvro toAvro(SwitchSensorEvent event);

    // Хаб
    @Mapping(source = "deviceType", target = "type")
    DeviceAddedEventAvro toAvro(DeviceAddedEvent event);

    DeviceRemovedEventAvro toAvro(DeviceRemovedEvent event);

    ScenarioAddedEventAvro toAvro(ScenarioAddedEvent event);

    ScenarioRemovedEventAvro toAvro(ScenarioRemovedEvent event);

    // Вложенные структуры
    DeviceActionAvro toAvro(DeviceAction action);

    ScenarioConditionAvro toAvro(ScenarioCondition condition);

    // Оборачиваем в SensorEventAvro
    default SensorEventAvro toSensorEventAvro(ClimateSensorEvent event) {
        return SensorEventAvro.newBuilder()
                .setId(event.getId())
                .setHubId(event.getHubId())
                .setTimestamp(event.getTimestamp())
                .setPayload(toAvro(event))
                .build();
    }

    default SensorEventAvro toSensorEventAvro(LightSensorEvent event) {
        return SensorEventAvro.newBuilder()
                .setId(event.getId())
                .setHubId(event.getHubId())
                .setTimestamp(event.getTimestamp())
                .setPayload(toAvro(event))
                .build();
    }

    default SensorEventAvro toSensorEventAvro(MotionSensorEvent event) {
        return SensorEventAvro.newBuilder()
                .setId(event.getId())
                .setHubId(event.getHubId())
                .setTimestamp(event.getTimestamp())
                .setPayload(toAvro(event))
                .build();
    }

    default SensorEventAvro toSensorEventAvro(TemperatureSensorEvent event) {
        return SensorEventAvro.newBuilder()
                .setId(event.getId())
                .setHubId(event.getHubId())
                .setTimestamp(event.getTimestamp())
                .setPayload(toAvro(event))
                .build();
    }

    default SensorEventAvro toSensorEventAvro(SwitchSensorEvent event) {
        return SensorEventAvro.newBuilder()
                .setId(event.getId())
                .setHubId(event.getHubId())
                .setTimestamp(event.getTimestamp())
                .setPayload(toAvro(event))
                .build();
    }

    // Оборачиваем в HubEventAvro
    default HubEventAvro toHubEventAvro(DeviceAddedEvent event) {
        return HubEventAvro.newBuilder()
                .setHubId(event.getHubId())
                .setTimestamp(event.getTimestamp())
                .setPayload(toAvro(event))
                .build();
    }

    default HubEventAvro toHubEventAvro(DeviceRemovedEvent event) {
        return HubEventAvro.newBuilder()
                .setHubId(event.getHubId())
                .setTimestamp(event.getTimestamp())
                .setPayload(toAvro(event))
                .build();
    }

    default HubEventAvro toHubEventAvro(ScenarioAddedEvent event) {
        return HubEventAvro.newBuilder()
                .setHubId(event.getHubId())
                .setTimestamp(event.getTimestamp())
                .setPayload(toAvro(event))
                .build();
    }

    default HubEventAvro toHubEventAvro(ScenarioRemovedEvent event) {
        return HubEventAvro.newBuilder()
                .setHubId(event.getHubId())
                .setTimestamp(event.getTimestamp())
                .setPayload(toAvro(event))
                .build();
    }

    default TypeDevice mapDeviceType(DeviceType deviceType) {
        if (deviceType == null) return null;
        return TypeDevice.valueOf(deviceType.name());
    }

    default TypeCondition mapConditionType(ConditionType conditionType) {
        if (conditionType == null) return null;
        return TypeCondition.valueOf(conditionType.name());
    }

    default Operation mapOperationType(OperationType operationType) {
        if (operationType == null) return null;
        return Operation.valueOf(operationType.name());
    }

    default TypeAction mapActionType(ActionType actionType) {
        if (actionType == null) return null;
        return TypeAction.valueOf(actionType.name());
    }
}