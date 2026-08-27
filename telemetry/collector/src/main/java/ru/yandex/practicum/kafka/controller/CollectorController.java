package ru.yandex.practicum.kafka.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.yandex.practicum.kafka.event.HubEvent;
import ru.yandex.practicum.kafka.event.SensorEvent;

/**
 * API для передачи событий от датчиков и хабов
 */
@RequestMapping("/events")
public interface CollectorController {

    /**
     * Эндпоинт для обработки событий от датчиков
     *
     * @param event - Данные события датчика (показания, изменение состояния и т.д)
     */
    @PostMapping("/sensors")
    void sensorsEvents(@RequestBody @Valid SensorEvent event);

    /**
     * Эндпоинт для обработки событий от хаба
     *
     * @param event - Данные события хаба (регистрация/удаление устройств в хабе, добавление/удаление сценария умного дома)
     */
    @PostMapping("/hubs")
    void hubsEvent(@RequestBody @Valid HubEvent event);
}
