package com.example.hotelbooking.controller;

import com.example.hotelbooking.dto.BookingRequestDTO;
import com.example.hotelbooking.dto.BookingResponseDTO;
import com.example.hotelbooking.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Управление бронированиями", description = "Методы для работы с бронированиями")
@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @Operation(summary = "Получить все бронирования", description = "Возвращает список всех бронирований")
    @GetMapping
    public ResponseEntity<List<BookingResponseDTO>> getAllBookings() {
        return ResponseEntity.ok(bookingService.findAll());
    }

    @Operation(summary = "Получить бронирование по ID", description = "Возвращает бронирование по его идентификатору")
    @GetMapping("/{id}")
    public ResponseEntity<BookingResponseDTO> getBookingById(
            @Parameter(description = "ID бронирования", required = true) @PathVariable Long id) {
        return ResponseEntity.ok(bookingService.findById(id));
    }

    @Operation(summary = "Получить бронирования гостя", description = "Возвращает все бронирования гостя по его ID")
    @GetMapping("/guest/{guestId}")
    public ResponseEntity<List<BookingResponseDTO>> getBookingsByGuestId(
            @Parameter(description = "ID гостя", required = true) @PathVariable Long guestId) {
        return ResponseEntity.ok(bookingService.findByGuestId(guestId));
    }

    @Operation(summary = "Создать бронирование", description = "Создаёт новое бронирование")
    @PostMapping
    public ResponseEntity<BookingResponseDTO> createBooking(@Valid @RequestBody BookingRequestDTO dto) {
        return new ResponseEntity<>(bookingService.create(dto), HttpStatus.CREATED);
    }

    @Operation(summary = "Обновить статус бронирования", description = "Обновляет статус бронирования")
    @PatchMapping("/{id}/status")
    public ResponseEntity<BookingResponseDTO> updateBookingStatus(
            @Parameter(description = "ID бронирования", required = true) @PathVariable Long id,
            @Parameter(description = "Новый статус", required = true) @RequestParam String status) {
        return ResponseEntity.ok(bookingService.updateStatus(id, status));
    }

    @Operation(summary = "Отменить бронирование", description = "Отменяет бронирование по ID")
    @PostMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelBooking(
            @Parameter(description = "ID бронирования", required = true) @PathVariable Long id) {
        bookingService.cancel(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Удалить отмененное бронирование", description = "Удаляет бронирование только со статусом CANCELLED")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCancelledBooking(
            @Parameter(description = "ID бронирования", required = true) @PathVariable Long id) {
        bookingService.deleteCancelled(id);
        return ResponseEntity.noContent().build();
    }
}
