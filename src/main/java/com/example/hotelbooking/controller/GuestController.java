package com.example.hotelbooking.controller;

import com.example.hotelbooking.dto.GuestRequestDTO;
import com.example.hotelbooking.dto.GuestResponseDTO;
import com.example.hotelbooking.service.GuestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.hotelbooking.dto.GuestWithBookingsDTO;
import java.util.List;

@RestController
@RequestMapping("/api/guests")
@RequiredArgsConstructor
public class GuestController {

    private final GuestService guestService;

    @GetMapping
    public ResponseEntity<List<GuestResponseDTO>> getAllGuests() {
        return ResponseEntity.ok(guestService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<GuestResponseDTO> getGuestById(@PathVariable Long id) {
        return ResponseEntity.ok(guestService.findById(id));
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<GuestResponseDTO> getGuestByEmail(@PathVariable String email) {
        return ResponseEntity.ok(guestService.findByEmail(email));
    }

    @PostMapping
    public ResponseEntity<GuestResponseDTO> createGuest(@RequestBody GuestRequestDTO dto) {
        return new ResponseEntity<>(guestService.create(dto), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<GuestResponseDTO> updateGuest(@PathVariable Long id, @RequestBody GuestRequestDTO dto) {
        return ResponseEntity.ok(guestService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGuest(@PathVariable Long id) {
        guestService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/with-bookings")
    public ResponseEntity<GuestResponseDTO> createGuestWithBookings(@RequestBody GuestWithBookingsDTO dto) {
        return new ResponseEntity<>(guestService.createGuestWithBookings(dto), HttpStatus.CREATED);
    }
}