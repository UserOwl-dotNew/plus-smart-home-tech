package ru.yandex.practicum.analyzer.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.yandex.practicum.analyzer.entity.Action;
import ru.yandex.practicum.analyzer.entity.Condition;
import ru.yandex.practicum.analyzer.entity.Sensor;
import ru.yandex.practicum.analyzer.enums.ActionType;
import ru.yandex.practicum.analyzer.enums.ConditionType;
import ru.yandex.practicum.analyzer.enums.OperationType;
import ru.yandex.practicum.kafka.telemetry.event.*;

@Mapper(componentModel = "spring")
public interface AvroToEntityMapper {

    @Mapping(target = "id", source = "avro.id")
    @Mapping(target = "hubId", source = "hubId")
    Sensor toSensor(DeviceAddedEventAvro avro, String hubId);

    Condition toCondition(ScenarioConditionAvro avro);

    Action toAction(DeviceActionAvro avro);

    /**
     * Дефолтный метод для маппинга авро в enum
     *
     * @param avro - тип условия для использования в сценариях
     * @return - enum
     */
    default ConditionType toConditionType(TypeCondition avro) {
        if (avro == null) return null;
        return ConditionType.valueOf(avro.name());
    }

    /**
     * Дефолтный метод для маппинга авро в enum
     *
     * @param avro - тип операции для условий
     * @return - enum
     */
    default OperationType toOperationType(Operation avro) {
        if (avro == null) return null;
        return OperationType.valueOf(avro.name());
    }

    /**
     * Дефолтный метод для маппинга авро в enum
     *
     * @param avro - тип действия при срабатывании условия активации сценария
     * @return - enum
     */
    default ActionType toActionType(TypeAction avro) {
        if (avro == null) return null;
        return ActionType.valueOf(avro.name());
    }
}
