package ru.yandex.practicum.kafka.event;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.yandex.practicum.kafka.enums.SensorEventType;

import static ru.yandex.practicum.kafka.enums.SensorEventType.TEMPERATURE_SENSOR_EVENT;

/*
 * Событие датчика температуры, содержащее информацию о температуре в градусах Цельсия и Фаренгейта.
 */
@Getter
@Setter
@ToString(callSuper = true)
public class TemperatureSensorEvent extends SensorEvent {
    private int temperatureC;
    private int temperatureF;

    @Override
    public SensorEventType getType() {
        return TEMPERATURE_SENSOR_EVENT;
    }
}
