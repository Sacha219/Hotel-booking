package com.example.hotelbooking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GuestWithBookingsDTO {
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private List<BookingRequestDTO> bookings;
}