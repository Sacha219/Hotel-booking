package com.example.hotelbooking.controller;

import com.example.hotelbooking.dto.GuestRequestDTO;
import com.example.hotelbooking.dto.GuestResponseDTO;
import com.example.hotelbooking.dto.GuestWithBookingsDTO;
import com.example.hotelbooking.service.GuestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Управление гостями", description = "Методы для работы с гостями")
@RestController
@RequestMapping("/api/guests")
@RequiredArgsConstructor
public class GuestController {

    private final GuestService guestService;

    @Operation(summary = "Получить всех гостей", description = "Возвращает список всех гостей")
    @GetMapping
    public ResponseEntity<List<GuestResponseDTO>> getAllGuests() {
        return ResponseEntity.ok(guestService.findAll());
    }

    @Operation(summary = "Получить гостя по ID", description = "Возвращает гостя по его идентификатору")
    @GetMapping("/{id}")
    public ResponseEntity<GuestResponseDTO> getGuestById(
            @Parameter(description = "ID гостя", required = true) @PathVariable Long id) {
        return ResponseEntity.ok(guestService.findById(id));
    }

    @Operation(summary = "Получить гостя по email", description = "Возвращает гостя по email")
    @GetMapping("/email/{email}")
    public ResponseEntity<GuestResponseDTO> getGuestByEmail(
            @Parameter(description = "Email гостя", required = true) @PathVariable String email) {
        return ResponseEntity.ok(guestService.findByEmail(email));
    }

    @Operation(summary = "Создать гостя", description = "Создаёт нового гостя")
    @PostMapping
    public ResponseEntity<GuestResponseDTO> createGuest(@Valid @RequestBody GuestRequestDTO dto) {
        return new ResponseEntity<>(guestService.create(dto), HttpStatus.CREATED);
    }

    @Operation(summary = "Обновить гостя", description = "Обновляет данные существующего гостя")
    @PutMapping("/{id}")
    public ResponseEntity<GuestResponseDTO> updateGuest(
            @Parameter(description = "ID гостя", required = true) @PathVariable Long id,
            @Valid @RequestBody GuestRequestDTO dto) {
        return ResponseEntity.ok(guestService.update(id, dto));
    }

    @Operation(summary = "Удалить гостя", description = "Удаляет гостя по ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGuest(
            @Parameter(description = "ID гостя", required = true) @PathVariable Long id) {
        guestService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Создать гостя с бронированиями", description = "Создаёт гостя и его бронирования в одном запросе")
    @PostMapping("/with-bookings")
    public ResponseEntity<GuestResponseDTO> createGuestWithBookings(@Valid @RequestBody GuestWithBookingsDTO dto) {
        return new ResponseEntity<>(guestService.createGuestWithBookings(dto), HttpStatus.CREATED);
    }
}