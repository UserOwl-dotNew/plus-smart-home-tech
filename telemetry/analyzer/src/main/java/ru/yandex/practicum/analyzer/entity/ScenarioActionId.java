package ru.yandex.practicum.analyzer.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;

@Data
@ToString
@EqualsAndHashCode
public class ScenarioActionId implements Serializable {
    private Scenario scenario;
    private Sensor sensor;
    private Action action;
}
