package ru.yandex.practicum.kafka.service;

import ru.yandex.practicum.kafka.event.HubEvent;
import ru.yandex.practicum.kafka.event.SensorEvent;

/**
 * Сервис для обработки событий телеметрии.
 * Преобразует входящие JSON-события в Avro-формат и отправляет в Kafka.
 */
public interface CollectorService {

    /**
     * Обрабатывает событие от датчика.
     * Маппит DTO → Avro и отправляет в топик telemetry.sensors.v1.
     *
     * @param event событие датчика (LightSensorEvent, TemperatureSensorEvent и т.д.)
     */
    void processSensorEvent(SensorEvent event);

    /**
     * Обрабатывает событие от хаба.
     * Маппит DTO → Avro и отправляет в топик telemetry.hubs.v1.
     *
     * @param event событие хаба (DeviceAddedEvent, ScenarioAddedEvent и т.д.)
     */
    void processHubEvent(HubEvent event);
}
