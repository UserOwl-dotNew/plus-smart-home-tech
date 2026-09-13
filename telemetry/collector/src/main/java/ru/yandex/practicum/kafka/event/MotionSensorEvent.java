package ru.yandex.practicum.kafka.event;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.yandex.practicum.kafka.enums.SensorEventType;

import static ru.yandex.practicum.kafka.enums.SensorEventType.MOTION_SENSOR_EVENT;

/*
 * Событие датчика движения.
 */
@Getter
@Setter
@ToString(callSuper = true)
public class MotionSensorEvent extends SensorEvent {
    private int linkQuality;
    private boolean motion;
    private int voltage;

    @Override
    public SensorEventType getType() {
        return MOTION_SENSOR_EVENT;
    }
}
