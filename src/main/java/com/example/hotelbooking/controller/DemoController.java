package com.example.hotelbooking.controller;

import com.example.hotelbooking.dto.BookingRequestDTO;
import com.example.hotelbooking.dto.GuestRequestDTO;
import com.example.hotelbooking.dto.HotelResponseDTO;
import com.example.hotelbooking.service.GuestService;
import com.example.hotelbooking.service.HotelService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/demo")
@RequiredArgsConstructor
public class DemoController {

    private final HotelService hotelService;
    private final GuestService guestService;

    @GetMapping("/nplus1/problem")
    public ResponseEntity<List<HotelResponseDTO>> demonstrateNPlusOneProblem() {
        List<HotelResponseDTO> hotels = hotelService.getAllHotels();
        return ResponseEntity.ok(hotels);
    }

    @GetMapping("/nplus1/solved")
    public ResponseEntity<List<HotelResponseDTO>> demonstrateNPlusOneSolved() {
        List<HotelResponseDTO> hotels = hotelService.getAllHotelsWithDetails();
        return ResponseEntity.ok(hotels);
    }

    @PostMapping("/guest-booking/without-tx")
    public ResponseEntity<String> createGuestAndBookingWithoutTx(@RequestBody Map<String, Object> payload) {
        try {
            GuestRequestDTO guestDto = new GuestRequestDTO();
            guestDto.setFirstName((String) payload.get("firstName"));
            guestDto.setLastName((String) payload.get("lastName"));
            guestDto.setEmail((String) payload.get("email"));
            guestDto.setPhone((String) payload.get("phone"));

            BookingRequestDTO bookingDto = new BookingRequestDTO();
            bookingDto.setRoomId(Long.valueOf(payload.get("roomId").toString()));
            bookingDto.setCheckInDate(java.time.LocalDate.parse((String) payload.get("checkIn")));
            bookingDto.setCheckOutDate(java.time.LocalDate.parse((String) payload.get("checkOut")));
            bookingDto.setStatus("CONFIRMED");

            guestService.createGuestAndBookingWithoutTransaction(guestDto, bookingDto);

            return ResponseEntity.ok("Данные успешно сохранены (не должно появиться)");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Ошибка: " + e.getMessage() + " — гость мог сохраниться, бронирование — нет");
        }
    }

    @PostMapping("/guest-booking/with-tx")
    public ResponseEntity<String> createGuestAndBookingWithTx(@RequestBody Map<String, Object> payload) {
        try {
            GuestRequestDTO guestDto = new GuestRequestDTO();
            guestDto.setFirstName((String) payload.get("firstName"));
            guestDto.setLastName((String) payload.get("lastName"));
            guestDto.setEmail((String) payload.get("email"));
            guestDto.setPhone((String) payload.get("phone"));

            BookingRequestDTO bookingDto = new BookingRequestDTO();
            bookingDto.setRoomId(Long.valueOf(payload.get("roomId").toString()));
            bookingDto.setCheckInDate(java.time.LocalDate.parse((String) payload.get("checkIn")));
            bookingDto.setCheckOutDate(java.time.LocalDate.parse((String) payload.get("checkOut")));
            bookingDto.setStatus("CONFIRMED");

            guestService.createGuestAndBookingWithTransaction(guestDto, bookingDto);

            return ResponseEntity.ok("Данные успешно сохранены (не должно появиться)");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Ошибка: " + e.getMessage() + " — всё откатилось, данные не сохранились");
        }
    }
}