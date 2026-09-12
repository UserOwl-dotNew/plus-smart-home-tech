package ru.yandex.practicum.analyzer.service;

import ru.yandex.practicum.kafka.telemetry.event.DeviceAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.DeviceRemovedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioRemovedEventAvro;

public interface HubEventService {
    /**
     * Обработка события добавления устройства
     *
     * @param event - событие (avro) по добавлению нового устройства
     */
    void handleDeviceAdded(DeviceAddedEventAvro event, String hubId);

    /**
     * Обработка события удаления устройства
     *
     * @param event - событие (avro) по удалению устройства
     */
    void handleDeviceRemoved(DeviceRemovedEventAvro event, String hubId);

    /**
     * Обработка события добавления сценария
     *
     * @param event - событие (avro) по добавлению нового сценария
     * @param hubId
     */
    void handleScenarioAdded(ScenarioAddedEventAvro event, String hubId);

    /**
     * Обработка события удаления сценария
     *
     * @param event - событие (avro) по удалению сценария
     * @param hubId
     */
    void handleScenarioRemoved(ScenarioRemovedEventAvro event, String hubId);
}
