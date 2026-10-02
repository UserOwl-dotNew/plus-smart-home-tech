package ru.yandex.practicum.inventory.service;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import ru.yandex.practicum.inventory.dto.InventoryDto;
import ru.yandex.practicum.inventory.dto.ReserveRequest;
import ru.yandex.practicum.inventory.dto.ReserveResponse;
import ru.yandex.practicum.inventory.dto.UpdateInventoryRequest;

import java.util.List;

public interface InventoryService {

    /**
     * Получить все товары
     *
     * @return - список dto
     */
    List<InventoryDto> getInventories();

    /**
     * Получить остатки по товару
     *
     * @param productId - id товара
     * @return - dto товара
     */
    InventoryDto getRemains(@Positive Long productId);

    /**
     * Создать складскую запись
     *
     * @param request - запрос, содержащий id и количество поступившего товара
     * @return - dto товара
     */
    InventoryDto createInventory(@Valid UpdateInventoryRequest request);

    /**
     * Обновить складскую запись
     *
     * @param request - запрос, содержащий id и количество поступившего товара
     * @return - dto товара
     */
    InventoryDto updateInventory(@Valid UpdateInventoryRequest request);

    /**
     * Зарезервировать товар
     *
     * @param request - запрос, содержащий id и желаемое количество товара, которое необходимо зарезервировать
     * @return - dto резервирования
     */
    ReserveResponse createReserve(@Valid ReserveRequest request);
}
