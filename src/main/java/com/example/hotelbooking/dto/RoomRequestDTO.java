package com.example.hotelbooking.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Объект передачи данных для создания/обновления номера")
public class RoomRequestDTO {

    @Schema(description = "Номер комнаты", example = "101", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Номер комнаты обязателен")
    @Size(min = 1, max = 10, message = "Номер комнаты должен быть от 1 до 10 символов")
    private String number;

    @Schema(description = "Этаж", example = "1")
    @Min(value = 1, message = "Этаж должен быть не меньше 1")
    private Integer floor;

    @Schema(description = "Вместимость", example = "2")
    @Min(value = 1, message = "Вместимость должна быть не меньше 1")
    private Integer capacity;

    @Schema(description = "Тип комнаты (STANDARD, DELUXE, SUITE)", example = "DELUXE")
    private String type;

    @Schema(description = "Цена за ночь", example = "150.0", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Цена обязательна")
    @Positive(message = "Цена должна быть больше 0")
    private Double price;

    @Schema(description = "Доступность", example = "true")
    private Boolean available;

    @Schema(description = "ID отеля", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "ID отеля обязателен")
    private Long hotelId;
}