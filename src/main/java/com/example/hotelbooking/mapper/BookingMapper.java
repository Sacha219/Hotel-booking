package com.example.hotelbooking.mapper;

import com.example.hotelbooking.dto.BookingRequestDTO;
import com.example.hotelbooking.dto.BookingResponseDTO;
import com.example.hotelbooking.entity.Booking;

public class BookingMapper {

    private BookingMapper() {

    }

    public static BookingResponseDTO toResponseDTO(Booking booking) {
        if (booking == null) {
            return null;
        }

        BookingResponseDTO dto = new BookingResponseDTO();
        dto.setId(booking.getId());
        dto.setCheckInDate(booking.getCheckInDate());
        dto.setCheckOutDate(booking.getCheckOutDate());
        dto.setTotalPrice(booking.getTotalPrice());
        dto.setStatus(booking.getStatus());

        if (booking.getRoom() != null) {
            dto.setRoomId(booking.getRoom().getId());
            dto.setRoomNumber(booking.getRoom().getRoomNumber());
        }

        if (booking.getGuest() != null) {
            dto.setGuestId(booking.getGuest().getId());
            dto.setGuestName(booking.getGuest().getFirstName() + " " + booking.getGuest().getLastName());
        }

        return dto;
    }

    public static Booking toEntity(BookingRequestDTO dto) {
        if (dto == null) {
            return null;
        }

        Booking booking = new Booking();
        booking.setCheckInDate(dto.getCheckInDate());
        booking.setCheckOutDate(dto.getCheckOutDate());
        booking.setStatus(dto.getStatus() != null ? dto.getStatus() : "PENDING");

        return booking;
    }
}