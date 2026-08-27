package ru.yandex.practicum.kafka.event;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.yandex.practicum.kafka.enums.DeviceType;

/*
 * Событие, сигнализирующее о добавлении нового устройства в систему.
 */
@Getter
@Setter
@ToString(callSuper = true)
public class DeviceAddedEvent extends HubEvent {
    private String id;
    private DeviceType deviceType;
}
