package ru.yandex.practicum.analyzer.service;

import ru.yandex.practicum.analyzer.entity.Scenario;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

/**
 * Сервис для проверки сценариев на основе снапшотов
 */
public interface ScenarioService {

    /**
     * Обработать снапшот - проверить все сценарии для хаба
     *
     * @param snapshot снапшот состояния хаба
     */
    void processSnapshot(SensorsSnapshotAvro snapshot);

    /**
     * Проверить один сценарий на выполнение
     *
     * @return true, если все условия выполнены
     */
    boolean checkScenario(Scenario scenario, SensorsSnapshotAvro snapshot);

    /**
     * Выполнить действия из сценария
     */
    void executeActions(Scenario scenario, SensorsSnapshotAvro snapshot);
}
