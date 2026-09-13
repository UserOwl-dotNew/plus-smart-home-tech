package ru.yandex.practicum.kafka.mapper;

import com.google.protobuf.Timestamp;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ru.yandex.practicum.grpc.telemetry.event.*;
import ru.yandex.practicum.kafka.telemetry.event.*;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface ProtoToAvroMapper {

    @Mapping(target = "timestamp", source = "timestamp", qualifiedByName = "protoToInstant")
    @Mapping(target = "payload", ignore = true)
    SensorEventAvro toSensorEventAvro(SensorEventProto proto);

    @Named("mapSensorPayload")
    default SensorEventAvro mapToSensorEventAvro(SensorEventProto proto) {
        if (proto == null) return null;

        SensorEventAvro.Builder builder = SensorEventAvro.newBuilder()
                .setId(proto.getId())
                .setHubId(proto.getHubId())
                .setTimestamp(protoToInstant(proto.getTimestamp()));

        switch (proto.getPayloadCase()) {
            case TEMPERATURE_SENSOR_PROTO:
                builder.setPayload(toTemperatureSensorAvro(proto.getTemperatureSensorProto()));
                break;
            case LIGHT_SENSOR_PROTO:
                builder.setPayload(toLightSensorAvro(proto.getLightSensorProto()));
                break;
            case MOTION_SENSOR_PROTO:
                builder.setPayload(toMotionSensorAvro(proto.getMotionSensorProto()));
                break;
            case CLIMATE_SENSOR_PROTO:
                builder.setPayload(toClimateSensorAvro(proto.getClimateSensorProto()));
                break;
            case SWITCH_SENSOR_PROTO:
                builder.setPayload(toSwitchSensorAvro(proto.getSwitchSensorProto()));
                break;
            default:
                throw new IllegalArgumentException("Unknown payload type: " + proto.getPayloadCase());
        }

        return builder.build();
    }

    TemperatureSensorEventAvro toTemperatureSensorAvro(TemperatureSensorProto proto);

    LightSensorEventAvro toLightSensorAvro(LightSensorProto proto);

    MotionSensorEventAvro toMotionSensorAvro(MotionSensorProto proto);

    ClimateSensorEventAvro toClimateSensorAvro(ClimateSensorProto proto);

    SwitchSensorEventAvro toSwitchSensorAvro(SwitchSensorProto proto);

    @Mapping(target = "timestamp", source = "timestamp", qualifiedByName = "protoToInstant")
    @Mapping(target = "payload", ignore = true)
    HubEventAvro toHubEventAvro(HubEventProto proto);

    @Named("mapHubPayload")
    default HubEventAvro mapToHubEventAvro(HubEventProto proto) {
        if (proto == null) return null;

        HubEventAvro.Builder builder = HubEventAvro.newBuilder()
                .setHubId(proto.getHubId())
                .setTimestamp(protoToInstant(proto.getTimestamp()));

        switch (proto.getPayloadCase()) {
            case DEVICE_ADDED_EVENT_PROTO:
                builder.setPayload(toDeviceAddedAvro(proto.getDeviceAddedEventProto()));
                break;
            case DEVICE_REMOVED_EVENT_PROTO:
                builder.setPayload(toDeviceRemovedAvro(proto.getDeviceRemovedEventProto()));
                break;
            case SCENARIO_ADDED_EVENT_PROTO:
                builder.setPayload(toScenarioAddedAvro(proto.getScenarioAddedEventProto()));
                break;
            case SCENARIO_REMOVED_EVENT_PROTO:
                builder.setPayload(toScenarioRemovedAvro(proto.getScenarioRemovedEventProto()));
                break;
            default:
                throw new IllegalArgumentException("Unknown payload type: " + proto.getPayloadCase());
        }

        return builder.build();
    }

    @Mapping(target = "type", source = "type", qualifiedByName = "mapDeviceType")
    DeviceAddedEventAvro toDeviceAddedAvro(DeviceAddedEventProto proto);

    DeviceRemovedEventAvro toDeviceRemovedAvro(DeviceRemovedEventProto proto);

    default ScenarioAddedEventAvro toScenarioAddedAvro(ScenarioAddedEventProto proto) {
        if (proto == null) return null;

        return ScenarioAddedEventAvro.newBuilder()
                .setName(proto.getName())
                .setConditions(toScenarioConditions(proto.getConditionsList()))
                .setActions(toDeviceActions(proto.getActionsList()))
                .build();
    }

    default List<ScenarioConditionAvro> toScenarioConditions(List<ScenarioConditionProto> conditions) {
        if (conditions == null) return null;
        return conditions.stream()
                .map(this::toScenarioConditionAvro)
                .collect(Collectors.toList());
    }

    default ScenarioConditionAvro toScenarioConditionAvro(ScenarioConditionProto proto) {
        if (proto == null) return null;

        return ScenarioConditionAvro.newBuilder()
                .setSensorId(proto.getSensorId())
                .setType(mapConditionType(proto.getType()))
                .setOperation(mapOperation(proto.getOperation()))
                .setValue(getConditionValue(proto))
                .build();
    }

    default List<DeviceActionAvro> toDeviceActions(List<DeviceActionProto> actions) {
        if (actions == null) return null;
        return actions.stream()
                .map(this::toDeviceActionAvro)
                .collect(Collectors.toList());
    }

    default DeviceActionAvro toDeviceActionAvro(DeviceActionProto proto) {
        if (proto == null) return null;

        return DeviceActionAvro.newBuilder()
                .setSensorId(proto.getSensorId())
                .setType(mapActionType(proto.getType()))
                .setValue(proto.hasValue() ? proto.getValue() : null)
                .build();
    }

    ScenarioRemovedEventAvro toScenarioRemovedAvro(ScenarioRemovedEventProto proto);

    default Integer getConditionValue(ScenarioConditionProto proto) {
        if (proto == null) return null;
        return switch (proto.getValueCase()) {
            case BOOL_VALUE -> proto.getBoolValue() ? 1 : 0;
            case INT_VALUE -> proto.getIntValue();
            default -> null;
        };
    }

    @Named("mapDeviceType")
    default TypeDevice mapDeviceType(DeviceTypeProto type) {
        if (type == null) return null;
        return TypeDevice.valueOf(type.name());
    }

    @Named("mapConditionType")
    default TypeCondition mapConditionType(ConditionTypeProto type) {
        if (type == null) return null;
        return TypeCondition.valueOf(type.name());
    }

    @Named("mapOperation")
    default Operation mapOperation(ConditionOperationProto operation) {
        if (operation == null) return null;
        return Operation.valueOf(operation.name());
    }

    @Named("mapActionType")
    default TypeAction mapActionType(ActionTypeProto type) {
        if (type == null) return null;
        return TypeAction.valueOf(type.name());
    }


    @Named("protoToInstant")
    default Instant protoToInstant(Timestamp timestamp) {
        if (timestamp == null) return Instant.now();
        return Instant.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos());
    }
}
