package com.example.hotelbooking.controller;

import com.example.hotelbooking.dto.RoomRequestDTO;
import com.example.hotelbooking.dto.RoomResponseDTO;
import com.example.hotelbooking.service.RoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
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

import java.time.LocalDate;
import java.util.List;

@Tag(name = "Управление номерами", description = "Методы для работы с номерами отелей")
@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    @Operation(summary = "Получить все номера", description = "Возвращает список всех номеров")
    @GetMapping
    public ResponseEntity<List<RoomResponseDTO>> getAllRooms() {
        return ResponseEntity.ok(roomService.findAll());
    }

    @Operation(summary = "Получить номер по ID", description = "Возвращает номер по его идентификатору")
    @GetMapping("/{id}")
    public ResponseEntity<RoomResponseDTO> getRoomById(
            @Parameter(description = "ID номера", required = true) @PathVariable Long id) {
        return ResponseEntity.ok(roomService.findById(id));
    }

    @Operation(summary = "Получить номера отеля", description = "Возвращает все номера указанного отеля")
    @GetMapping("/hotel/{hotelId}")
    public ResponseEntity<List<RoomResponseDTO>> getRoomsByHotelId(
            @Parameter(description = "ID отеля", required = true) @PathVariable Long hotelId) {
        return ResponseEntity.ok(roomService.findByHotelId(hotelId));
    }

    @Operation(summary = "Найти доступные номера", description = "Поиск доступных номеров на указанные даты")
    @GetMapping("/available")
    public ResponseEntity<List<RoomResponseDTO>> getAvailableRooms(
            @Parameter(description = "ID отеля", required = true) @RequestParam Long hotelId,
            @Parameter(description = "Дата заезда", required = true) @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @Parameter(description = "Дата выезда", required = true) @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut) {
        return ResponseEntity.ok(roomService.findAvailableRooms(hotelId, checkIn, checkOut));
    }

    @Operation(summary = "Создать номер", description = "Создаёт новый номер")
    @PostMapping
    public ResponseEntity<RoomResponseDTO> createRoom(@Valid @RequestBody RoomRequestDTO dto) {
        return new ResponseEntity<>(roomService.create(dto), HttpStatus.CREATED);
    }

    @Operation(summary = "Обновить номер", description = "Обновляет данные существующего номера")
    @PutMapping("/{id}")
    public ResponseEntity<RoomResponseDTO> updateRoom(
            @Parameter(description = "ID номера", required = true) @PathVariable Long id,
            @Valid @RequestBody RoomRequestDTO dto) {
        return ResponseEntity.ok(roomService.update(id, dto));
    }

    @Operation(summary = "Удалить номер", description = "Удаляет номер по ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoom(
            @Parameter(description = "ID номера", required = true) @PathVariable Long id) {
        roomService.delete(id);
        return ResponseEntity.noContent().build();
    }
}