package com.example.hotelbooking.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Объект передачи данных для создания/обновления отеля")
public class HotelRequestDTO {

    @Schema(description = "Название отеля", example = "Grand Hotel Minsk", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Название отеля обязательно")
    @Size(min = 2, max = 100, message = "Название должно быть от 2 до 100 символов")
    private String name;

    @Schema(description = "Адрес отеля", example = "пр-т Независимости, 15")
    private String address;

    @Schema(description = "Город", example = "Минск", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Город обязателен")
    @Size(max = 50, message = "Название города не должно превышать 50 символов")
    private String city;

    @Schema(description = "Количество звёзд (1-5)", example = "5")
    @Min(value = 1, message = "Минимум 1 звезда")
    @Max(value = 5, message = "Максимум 5 звёзд")
    private Integer stars;

    @Schema(description = "Описание отеля", example = "Роскошный отель в центре Минска")
    @Size(max = 500, message = "Описание не должно превышать 500 символов")
    private String description;

    @Schema(description = "Ссылка на фото отеля или data URL")
    private String imageUrl;

    @Schema(description = "Цена за ночь", example = "150.0", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Цена за ночь обязательна")
    @DecimalMin(value = "0.0", inclusive = false, message = "Цена должна быть больше 0")
    private Double pricePerNight;

    @Schema(description = "Доступность", example = "true")
    private Boolean available;
}
