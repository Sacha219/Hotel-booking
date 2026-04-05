package com.example.hotelbooking.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Объект передачи данных для создания/обновления гостя")
public class GuestRequestDTO {

    @Schema(description = "Имя гостя", example = "Александра", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Имя обязательно")
    @Size(min = 2, max = 50, message = "Имя должно быть от 2 до 50 символов")
    private String firstName;

    @Schema(description = "Фамилия гостя", example = "Лукашевич", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Фамилия обязательна")
    @Size(min = 2, max = 50, message = "Фамилия должна быть от 2 до 50 символов")
    private String lastName;

    @Schema(description = "Email гостя", example = "sasha@email.com", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Email обязателен")
    @Email(message = "Некорректный формат email")
    private String email;

    @Schema(description = "Телефон гостя", example = "+375-29-123-45-67")
    @Pattern(regexp = "^\\+?[0-9\\-\\s]{10,20}$", message = "Некорректный формат телефона")
    private String phone;
}