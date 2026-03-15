package com.example.hotelbooking.controller;

import com.example.hotelbooking.dto.GuestRequestDTO;
import com.example.hotelbooking.dto.HotelResponseDTO;
import com.example.hotelbooking.service.BookingService;
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

@RestController
@RequestMapping("/api/demo")
@RequiredArgsConstructor
public class DemoController {

    private final HotelService hotelService;
    private final BookingService bookingService;
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

    @PostMapping("/transaction/without")
    public ResponseEntity<String> testWithoutTransaction(@RequestBody GuestRequestDTO dto) {
        try {
            guestService.createWithoutTransaction(dto);
            return ResponseEntity.ok("Данные сохранились? Проверь БД");
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Ошибка: " + e.getMessage() + " - данные могли сохраниться частично");
        }
    }

    @PostMapping("/transaction/with")
    public ResponseEntity<String> testWithTransaction(@RequestBody GuestRequestDTO dto) {
        try {
            guestService.createWithTransaction(dto);
            return ResponseEntity.ok("Это сообщение не должно появиться");
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Ошибка: " + e.getMessage() + " - всё откатилось, данные не сохранились");
        }
    }
}