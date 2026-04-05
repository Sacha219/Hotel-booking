package com.example.hotelbooking.controller;

import com.example.hotelbooking.dto.HotelRequestDTO;
import com.example.hotelbooking.dto.HotelResponseDTO;
import com.example.hotelbooking.service.HotelCachingService;
import com.example.hotelbooking.service.HotelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Управление отелями", description = "Методы для работы с отелями")
@RestController
@RequestMapping("/api/hotels")
@RequiredArgsConstructor
public class HotelController {

    private final HotelService hotelService;
    private final HotelCachingService hotelCachingService;

    @Operation(summary = "Создать отель", description = "Создаёт новый отель")
    @PostMapping
    public ResponseEntity<HotelResponseDTO> createHotel(@Valid @RequestBody HotelRequestDTO hotelRequestDTO) {
        HotelResponseDTO createdHotel = hotelService.createHotel(hotelRequestDTO);
        return new ResponseEntity<>(createdHotel, HttpStatus.CREATED);
    }

    @Operation(summary = "Получить все отели (без оптимизации)", description = "Демонстрация проблемы N+1")
    @GetMapping("/plain")
    public ResponseEntity<List<HotelResponseDTO>> getAllHotelsPlain() {
        return ResponseEntity.ok(hotelService.getAllHotelsPlain());
    }

    @Operation(summary = "Получить все отели с деталями", description = "Демонстрация решения N+1")
    @GetMapping("/with-details")
    public ResponseEntity<List<HotelResponseDTO>> getAllHotelsWithDetails() {
        return ResponseEntity.ok(hotelService.getAllHotelsWithDetails());
    }

    @Operation(summary = "Получить отель по ID", description = "Возвращает отель по его идентификатору")
    @GetMapping("/{id}")
    public ResponseEntity<HotelResponseDTO> getHotelById(
            @Parameter(description = "ID отеля", required = true) @PathVariable Long id) {
        return ResponseEntity.ok(hotelService.getHotelById(id));
    }

    @Operation(summary = "Получить все отели с фильтрацией", description = "Фильтрация по городу и звёздам")
    @GetMapping
    public ResponseEntity<List<HotelResponseDTO>> getHotels(
            @Parameter(description = "Город") @RequestParam(required = false) String city,
            @Parameter(description = "Количество звёзд") @RequestParam(required = false) Integer stars) {

        List<HotelResponseDTO> hotels;

        if (city != null && stars != null) {
            hotels = hotelService.getHotelsByCityAndStars(city, stars);
        } else if (city != null) {
            hotels = hotelService.getHotelsByCity(city);
        } else if (stars != null) {
            hotels = hotelService.getHotelsByStars(stars);
        } else {
            hotels = hotelService.getAllHotelsWithDetails();
        }

        return ResponseEntity.ok(hotels);
    }

    @Operation(summary = "Обновить отель", description = "Обновляет данные существующего отеля")
    @PutMapping("/{id}")
    public ResponseEntity<HotelResponseDTO> updateHotel(
            @Parameter(description = "ID отеля", required = true) @PathVariable Long id,
            @Valid @RequestBody HotelRequestDTO dto) {
        return ResponseEntity.ok(hotelService.updateHotel(id, dto));
    }

    @Operation(summary = "Удалить отель", description = "Удаляет отель по ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHotel(
            @Parameter(description = "ID отеля", required = true) @PathVariable Long id) {
        hotelService.deleteHotel(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Поиск отелей по типу комнаты и цене", description = "JPQL запрос с пагинацией")
    @GetMapping("/filter")
    public ResponseEntity<Page<HotelResponseDTO>> filterHotelsByRoomTypeAndPrice(
            @Parameter(description = "Тип комнаты (STANDARD, DELUXE, SUITE)", required = true)
            @RequestParam String roomType,
            @Parameter(description = "Минимальная цена", required = true)
            @RequestParam Double minPrice,
            @Parameter(description = "Номер страницы") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Размер страницы") @RequestParam(defaultValue = "10") int size) {

        Page<HotelResponseDTO> result = hotelService.findHotelsByRoomTypeAndPrice(roomType, minPrice, page, size);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Поиск отелей (Native SQL)", description = "Native SQL запрос с пагинацией")
    @GetMapping("/filter/native")
    public ResponseEntity<Page<HotelResponseDTO>> filterHotelsByRoomTypeAndPriceNative(
            @Parameter(description = "Тип комнаты (STANDARD, DELUXE, SUITE)", required = true)
            @RequestParam String roomType,
            @Parameter(description = "Минимальная цена", required = true)
            @RequestParam Double minPrice,
            @Parameter(description = "Номер страницы") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Размер страницы") @RequestParam(defaultValue = "10") int size) {

        Page<HotelResponseDTO> result = hotelCachingService.findHotelsByRoomTypeAndPriceNative(roomType, minPrice, page, size);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Очистить кэш", description = "Очищает in-memory кэш")
    @PostMapping("/cache/clear")
    public ResponseEntity<String> clearCache() {
        hotelCachingService.invalidateAll();
        return ResponseEntity.ok("Кэш очищен");
    }
}