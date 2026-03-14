package com.example.hotelbooking.mapper;

import com.example.hotelbooking.dto.GuestRequestDTO;
import com.example.hotelbooking.dto.GuestResponseDTO;
import com.example.hotelbooking.entity.Guest;
import com.example.hotelbooking.entity.Booking;
import java.time.LocalDate;
import java.util.stream.Collectors;

public class GuestMapper {

    private GuestMapper() {

    }

    public static GuestResponseDTO toResponseDTO(Guest guest) {
        if (guest == null) {
            return null;
        }

        GuestResponseDTO dto = new GuestResponseDTO();
        dto.setId(guest.getId());
        dto.setFirstName(guest.getFirstName());
        dto.setLastName(guest.getLastName());
        dto.setEmail(guest.getEmail());
        dto.setPhone(guest.getPhone());
        dto.setRegistrationDate(guest.getRegistrationDate());

        if (guest.getBookings() != null && !guest.getBookings().isEmpty()) {
            dto.setBookingIds(guest.getBookings().stream()
                    .map(Booking::getId)
                    .collect(Collectors.toList()));
        }

        return dto;
    }

    public static Guest toEntity(GuestRequestDTO dto) {
        if (dto == null) {
            return null;
        }

        Guest guest = new Guest();
        guest.setFirstName(dto.getFirstName());
        guest.setLastName(dto.getLastName());
        guest.setEmail(dto.getEmail());
        guest.setPhone(dto.getPhone());
        guest.setRegistrationDate(LocalDate.now());

        return guest;
    }
}