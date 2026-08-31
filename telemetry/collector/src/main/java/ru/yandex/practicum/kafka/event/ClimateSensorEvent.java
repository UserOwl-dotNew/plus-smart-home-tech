package ru.yandex.practicum.kafka.event;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.yandex.practicum.kafka.enums.SensorEventType;

import static ru.yandex.practicum.kafka.enums.SensorEventType.CLIMATE_SENSOR_EVENT;

/*
 * Событие климатического датчика, содержащее информацию о температуре, влажности и уровне CO2.
 */
@Getter
@Setter
@ToString(callSuper = true)
public class ClimateSensorEvent extends SensorEvent {
    private int temperatureC;
    private int humidity;
    private int co2Level;

    @Override
    public SensorEventType getType() {
        return CLIMATE_SENSOR_EVENT;
    }
}
