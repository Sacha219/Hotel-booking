package com.example.hotelbooking.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HotelRequestDTO {

    @NotBlank(message = "Название отеля обязательно")
    @Size(min = 2, max = 100, message = "Название должно быть от 2 до 100 символов")
    private String name;

    @NotBlank(message = "Адрес обязателен")
    @Size(max = 200, message = "Адрес не должен превышать 200 символов")
    private String address;

    @NotBlank(message = "Город обязателен")
    @Size(max = 50, message = "Название города не должно превышать 50 символов")
    private String city;

    @NotBlank(message = "Страна обязательна")
    @Size(max = 50, message = "Название страны не должно превышать 50 символов")
    private String country;

    @Min(value = 1, message = "Минимум 1 звезда")
    @Max(value = 5, message = "Максимум 5 звёзд")
    private Integer stars;

    @Size(max = 500, message = "Описание не должно превышать 500 символов")
    private String description;

    @NotNull(message = "Цена за ночь обязательна")
    @DecimalMin(value = "0.0", inclusive = false, message = "Цена должна быть больше 0")
    private Double pricePerNight;

    private Boolean available;
}