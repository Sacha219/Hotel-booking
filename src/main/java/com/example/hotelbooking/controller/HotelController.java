package com.example.hotelbooking.controller;

import com.example.hotelbooking.dto.HotelResponseDTO;
import com.example.hotelbooking.service.HotelService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/hotels")
@RequiredArgsConstructor
public class HotelController {

    private final HotelService hotelService;

    @GetMapping("/{id}")
    public ResponseEntity<HotelResponseDTO> getHotelById(@PathVariable Long id) {
        HotelResponseDTO hotel = hotelService.getHotelById(id);
        return ResponseEntity.ok(hotel);
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
            hotels = hotelService.getAllHotels();
        }

        return ResponseEntity.ok(hotels);
    }
}