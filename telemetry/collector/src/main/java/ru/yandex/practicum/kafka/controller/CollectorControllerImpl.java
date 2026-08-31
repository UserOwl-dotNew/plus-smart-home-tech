package ru.yandex.practicum.kafka.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.kafka.event.HubEvent;
import ru.yandex.practicum.kafka.event.SensorEvent;
import ru.yandex.practicum.kafka.service.CollectorService;

@Slf4j
@RestController
@RequiredArgsConstructor
public class CollectorControllerImpl implements CollectorController {
    private final CollectorService service;

    @Override
    public void sensorsEvents(@RequestBody @Valid SensorEvent event) {
        log.info("Получено событие датчика: " + event);
        service.processSensorEvent(event);
    }

    @Override
    public void hubsEvent(@RequestBody @Valid HubEvent event) {
        log.info("Получено событие хаба: " + event);
        service.processHubEvent(event);
    }
}

