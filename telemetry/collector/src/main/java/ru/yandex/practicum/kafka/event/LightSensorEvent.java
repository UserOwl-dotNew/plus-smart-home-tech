package ru.yandex.practicum.kafka.event;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.yandex.practicum.kafka.enums.SensorEventType;

import static ru.yandex.practicum.kafka.enums.SensorEventType.LIGHT_SENSOR_EVENT;

/*
 * Событие датчика освещенности, содержащее информацию о качестве связи и уровне освещенности.
 */
@Getter
@Setter
@ToString(callSuper = true)
public class LightSensorEvent extends SensorEvent {
    private int linkQuality;
    private int luminosity;

    @Override
    public SensorEventType getType() {
        return LIGHT_SENSOR_EVENT;
    }
}
