package ru.yandex.practicum.kafka.event;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.yandex.practicum.kafka.enums.ConditionType;
import ru.yandex.practicum.kafka.enums.OperationType;

/*
 * Условие сценария, которое содержит информацию о датчике, типе условия, операции и значении.
 */
@Getter
@Setter
@ToString
public class ScenarioCondition {
    private String sensorId;
    private ConditionType type;
    private OperationType operation;
    private int value;
}
