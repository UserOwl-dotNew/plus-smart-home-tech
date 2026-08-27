package ru.yandex.practicum.kafka.event;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/*
 * Событие удаления сценария из системы. Содержит информацию о названии удаленного сценария.
 */
@Getter
@Setter
@ToString(callSuper = true)
public class ScenarioRemovedEvent extends HubEvent {
    private String name;
}
