package com.example.hotelbooking.controller;

import com.example.hotelbooking.dto.HotelRequestDTO;
import com.example.hotelbooking.dto.HotelResponseDTO;
import com.example.hotelbooking.service.HotelCachingService;
import com.example.hotelbooking.service.HotelService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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
    private final HotelCachingService hotelCachingService;

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

    @PutMapping("/{id}")
    public ResponseEntity<HotelResponseDTO> updateHotel(@PathVariable Long id, @RequestBody HotelRequestDTO dto) {
        return ResponseEntity.ok(hotelService.updateHotel(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHotel(@PathVariable Long id) {
        hotelService.deleteHotel(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/filter")
    public ResponseEntity<Page<HotelResponseDTO>> filterHotelsByRoomTypeAndPrice(
            @RequestParam String roomType,
            @RequestParam Double minPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<HotelResponseDTO> result = hotelService.findHotelsByRoomTypeAndPrice(roomType, minPrice, page, size);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/filter/native")
    public ResponseEntity<Page<HotelResponseDTO>> filterHotelsByRoomTypeAndPriceNative(
            @RequestParam String roomType,
            @RequestParam Double minPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<HotelResponseDTO> result = hotelCachingService.findHotelsByRoomTypeAndPriceNative(roomType, minPrice, page, size);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/cache/clear")
    public ResponseEntity<String> clearCache() {
        hotelCachingService.invalidateAll();
        return ResponseEntity.ok("Кэш очищен");
    }
}