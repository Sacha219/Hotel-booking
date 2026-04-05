package com.example.hotelbooking.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Объект передачи данных для создания бронирования")
public class BookingRequestDTO {

    @Schema(description = "Дата заезда", example = "2026-05-10", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Дата заезда обязательна")
    @Future(message = "Дата заезда должна быть в будущем")
    private LocalDate checkInDate;

    @Schema(description = "Дата выезда", example = "2026-05-15", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Дата выезда обязательна")
    @Future(message = "Дата выезда должна быть в будущем")
    private LocalDate checkOutDate;

    @Schema(description = "ID комнаты", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "ID комнаты обязателен")
    private Long roomId;

    @Schema(description = "ID гостя", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "ID гостя обязателен")
    private Long guestId;

    @Schema(description = "Статус бронирования", example = "CONFIRMED")
    private String status;
}