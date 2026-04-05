package com.example.hotelbooking.controller;

import com.example.hotelbooking.dto.AmenityDTO;
import com.example.hotelbooking.service.AmenityService;
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

@Tag(name = "Управление удобствами", description = "Методы для работы с удобствами отелей")
@RestController
@RequestMapping("/api/amenities")
@RequiredArgsConstructor
public class AmenityController {

    private final AmenityService amenityService;

    @Operation(summary = "Получить все удобства", description = "Возвращает список всех удобств")
    @GetMapping
    public ResponseEntity<List<AmenityDTO>> getAllAmenities() {
        return ResponseEntity.ok(amenityService.findAll());
    }

    @Operation(summary = "Получить удобство по ID", description = "Возвращает удобство по его идентификатору")
    @GetMapping("/{id}")
    public ResponseEntity<AmenityDTO> getAmenityById(
            @Parameter(description = "ID удобства", required = true) @PathVariable Long id) {
        return ResponseEntity.ok(amenityService.findById(id));
    }

    @Operation(summary = "Получить удобство по названию", description = "Возвращает удобство по названию")
    @GetMapping("/name/{name}")
    public ResponseEntity<AmenityDTO> getAmenityByName(
            @Parameter(description = "Название удобства", required = true) @PathVariable String name) {
        return ResponseEntity.ok(amenityService.findByName(name));
    }

    @Operation(summary = "Создать удобство", description = "Создаёт новое удобство")
    @PostMapping
    public ResponseEntity<AmenityDTO> createAmenity(@Valid @RequestBody AmenityDTO dto) {
        return new ResponseEntity<>(amenityService.create(dto), HttpStatus.CREATED);
    }

    @Operation(summary = "Обновить удобство", description = "Обновляет данные существующего удобства")
    @PutMapping("/{id}")
    public ResponseEntity<AmenityDTO> updateAmenity(
            @Parameter(description = "ID удобства", required = true) @PathVariable Long id,
            @Valid @RequestBody AmenityDTO dto) {
        return ResponseEntity.ok(amenityService.update(id, dto));
    }

    @Operation(summary = "Удалить удобство", description = "Удаляет удобство по ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAmenity(
            @Parameter(description = "ID удобства", required = true) @PathVariable Long id) {
        amenityService.delete(id);
        return ResponseEntity.noContent().build();
    }
}