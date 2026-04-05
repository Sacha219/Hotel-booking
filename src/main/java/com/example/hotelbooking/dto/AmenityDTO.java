package com.example.hotelbooking.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Объект передачи данных для удобства")
public class AmenityDTO {

    @Schema(description = "Уникальный идентификатор", example = "1")
    private Long id;

    @Schema(description = "Название удобства", example = "WiFi", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Название удобства обязательно")
    @Size(min = 2, max = 50, message = "Название должно быть от 2 до 50 символов")
    private String name;

    @Schema(description = "Описание", example = "Бесплатный высокоскоростной интернет")
    private String description;

    @Schema(description = "Иконка", example = "wifi")
    private String icon;
}