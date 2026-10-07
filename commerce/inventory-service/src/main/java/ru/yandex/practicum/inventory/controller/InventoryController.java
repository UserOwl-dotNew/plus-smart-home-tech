package ru.yandex.practicum.inventory.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.inventory.dto.InventoryDto;
import ru.yandex.practicum.inventory.dto.ReserveRequest;
import ru.yandex.practicum.inventory.dto.ReserveResponse;
import ru.yandex.practicum.inventory.dto.UpdateInventoryRequest;
import ru.yandex.practicum.inventory.service.InventoryService;

import java.util.List;

@Slf4j
@Validated
@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {
    private final InventoryService service;

    @GetMapping
    public List<InventoryDto> getAll() {
        log.info("GET /api/inventory - запрос на получение всех записей инвентаря");
        List<InventoryDto> result = service.getInventories();
        log.info("GET /api/inventory - возвращено записей: {}", result.size());
        return result;
    }

    @GetMapping("/{productId}")
    public InventoryDto get(@PathVariable @Positive Long productId) {
        log.info("GET /api/inventory/{} - запрос на получение остатков", productId);
        InventoryDto result = service.getRemains(productId);
        log.info("GET /api/inventory/{} - возвращено: {}", productId, result);
        return result;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public InventoryDto add(@RequestBody @Valid UpdateInventoryRequest request) {
        log.info("POST /api/inventory - запрос на создание записи: {}", request);
        InventoryDto result = service.createInventory(request);
        log.info("POST /api/inventory - создана запись: {}", result);
        return result;
    }

    @PutMapping
    public InventoryDto update(@RequestBody @Valid UpdateInventoryRequest request) {
        log.info("PUT /api/inventory - запрос на обновление записи: {}", request);
        InventoryDto result = service.updateInventory(request);
        log.info("PUT /api/inventory - обновлена запись: {}", result);
        return result;
    }

    @PostMapping("/reserve")
    public ReserveResponse reserve(@RequestBody @Valid ReserveRequest request) {
        log.info("POST /api/inventory/reserve - запрос на резервирование: {}", request);
        ReserveResponse result = service.createReserve(request);
        log.info("POST /api/inventory/reserve - результат резервирования: {}", result);
        return result;
    }

    @PostMapping("/release")
    public ReserveResponse release(@RequestBody @Valid ReserveRequest request) {
        log.info("POST /api/inventory/release - запрос на снятие резерва: {}", request);
        ReserveResponse result = service.createReserve(request);
        log.info("POST /api/inventory/release - результат снятия резерва: {}", result);
        return result;
    }
}