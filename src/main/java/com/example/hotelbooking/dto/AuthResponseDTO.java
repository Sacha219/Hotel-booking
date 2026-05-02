package com.example.hotelbooking.dto;

import com.example.hotelbooking.entity.UserRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponseDTO {

    private Long id;
    private UserRole role;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private Long guestId;
}
