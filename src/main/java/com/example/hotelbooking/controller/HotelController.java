package com.example.hotelbooking.controller;

import com.example.hotelbooking.dto.HotelRequestDTO;
import com.example.hotelbooking.dto.HotelResponseDTO;
import com.example.hotelbooking.service.HotelService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/hotels")
@RequiredArgsConstructor
public class HotelController {

    private final HotelService hotelService;

    @PostMapping
    public ResponseEntity<HotelResponseDTO> createHotel(@RequestBody HotelRequestDTO hotelRequestDTO) {
        HotelResponseDTO createdHotel = hotelService.createHotel(hotelRequestDTO);
        return new ResponseEntity<>(createdHotel, HttpStatus.CREATED);
    }

    @GetMapping("/plain")
    public ResponseEntity<List<HotelResponseDTO>> getAllHotelsPlain() {
        return ResponseEntity.ok(hotelService.getAllHotelsPlain());
    }

    @GetMapping("/with-details")
    public ResponseEntity<List<HotelResponseDTO>> getAllHotelsWithDetails() {
        return ResponseEntity.ok(hotelService.getAllHotelsWithDetails());
    }

    @GetMapping("/{id}")
    public ResponseEntity<HotelResponseDTO> getHotelById(@PathVariable Long id) {
        return ResponseEntity.ok(hotelService.getHotelById(id));
    }

    @GetMapping
    public ResponseEntity<List<HotelResponseDTO>> getHotels(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) Integer stars) {

        List<HotelResponseDTO> hotels;

        if (city != null && stars != null) {
            hotels = hotelService.getHotelsByCityAndStars(city, stars);
        } else if (city != null) {
            hotels = hotelService.getHotelsByCity(city);
        } else if (stars != null) {
            hotels = hotelService.getHotelsByStars(stars);
        } else {
            hotels = hotelService.getAllHotelsWithDetails();
        }

        return ResponseEntity.ok(hotels);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHotel(@PathVariable Long id) {
        hotelService.deleteHotel(id);
        return ResponseEntity.noContent().build();
    }
}