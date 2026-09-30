package ru.yandex.practicum.inventory.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.inventory.dto.InventoryDto;
import ru.yandex.practicum.inventory.dto.ReserveRequest;
import ru.yandex.practicum.inventory.dto.ReserveResponse;
import ru.yandex.practicum.inventory.dto.UpdateInventoryRequest;
import ru.yandex.practicum.inventory.service.InventoryService;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {
    private final InventoryService service;

    @GetMapping
    public List<InventoryDto> getAll() {
        return service.getInventories();
    }

    @GetMapping("/{productId}")
    public InventoryDto get(@PathVariable @Positive Long productId) {
        return service.getRemains(productId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public InventoryDto add(@RequestBody @Valid UpdateInventoryRequest request) {
        return service.createInventory(request);
    }

    @PutMapping
    public InventoryDto update(@RequestBody @Valid UpdateInventoryRequest request) {
        return service.updateInventory(request);
    }

    @PostMapping("/reserve")
    public ReserveResponse addReserve(@RequestBody @Valid ReserveRequest request) {
        return service.createReserve(request);
    }
}
