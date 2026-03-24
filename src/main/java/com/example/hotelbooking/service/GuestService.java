package com.example.hotelbooking.service;

import com.example.hotelbooking.dto.BookingRequestDTO;
import com.example.hotelbooking.dto.GuestRequestDTO;
import com.example.hotelbooking.dto.GuestResponseDTO;
import com.example.hotelbooking.entity.Booking;
import com.example.hotelbooking.entity.Guest;
import com.example.hotelbooking.exception.TransactionDemoException;
import com.example.hotelbooking.mapper.BookingMapper;
import com.example.hotelbooking.mapper.GuestMapper;
import com.example.hotelbooking.repository.BookingRepository;
import com.example.hotelbooking.repository.GuestRepository;
import com.example.hotelbooking.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class GuestService {

    private final GuestRepository guestRepository;
    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;

    @Transactional(readOnly = true)
    public List<GuestResponseDTO> findAll() {
        return guestRepository.findAll().stream()
                .map(GuestMapper::toResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public GuestResponseDTO findById(Long id) {
        Guest guest = guestRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Guest not found with id: " + id));
        return GuestMapper.toResponseDTO(guest);
    }

    @Transactional(readOnly = true)
    public GuestResponseDTO findByEmail(String email) {
        Guest guest = guestRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new NoSuchElementException("Guest not found with email: " + email));
        return GuestMapper.toResponseDTO(guest);
    }

    @Transactional
    public GuestResponseDTO create(GuestRequestDTO dto) {
        if (guestRepository.existsByEmailIgnoreCase(dto.getEmail())) {
            throw new IllegalArgumentException("Guest with email " + dto.getEmail() + " already exists");
        }

        Guest guest = GuestMapper.toEntity(dto);
        guestRepository.save(guest);
        return GuestMapper.toResponseDTO(guest);
    }

    public void createGuestAndBookingWithoutTransaction(GuestRequestDTO guestDto, BookingRequestDTO bookingDto) {

        Guest guest = GuestMapper.toEntity(guestDto);
        guestRepository.save(guest);

        Booking booking = BookingMapper.toEntity(bookingDto);
        booking.setGuest(guest);

        var room = roomRepository.findById(bookingDto.getRoomId())
                .orElseThrow(() -> new RuntimeException("Room not found"));
        booking.setRoom(room);

        long nights = ChronoUnit.DAYS.between(bookingDto.getCheckInDate(), bookingDto.getCheckOutDate());
        booking.setTotalPrice(room.getPrice() * nights);

        bookingRepository.save(booking);

        throw new TransactionDemoException("Ошибка после сохранения гостя и бронирования (БЕЗ @Transactional)");
    }

    @Transactional
    public void createGuestAndBookingWithTransaction(GuestRequestDTO guestDto, BookingRequestDTO bookingDto) {

        Guest guest = GuestMapper.toEntity(guestDto);
        guestRepository.save(guest);

        Booking booking = BookingMapper.toEntity(bookingDto);
        booking.setGuest(guest);

        var room = roomRepository.findById(bookingDto.getRoomId())
                .orElseThrow(() -> new RuntimeException("Room not found"));
        booking.setRoom(room);

        long nights = ChronoUnit.DAYS.between(bookingDto.getCheckInDate(), bookingDto.getCheckOutDate());
        booking.setTotalPrice(room.getPrice() * nights);

        bookingRepository.save(booking);

        throw new TransactionDemoException("Ошибка после сохранения гостя и бронирования (С @Transactional)");
    }

    @Transactional
    public GuestResponseDTO update(Long id, GuestRequestDTO dto) {
        Guest guest = guestRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Guest not found with id:" + id));

        guest.setFirstName(dto.getFirstName());
        guest.setLastName(dto.getLastName());
        guest.setPhone(dto.getPhone());

        if (!guest.getEmail().equalsIgnoreCase(dto.getEmail())) {
            if (guestRepository.existsByEmailIgnoreCase(dto.getEmail())) {
                throw new IllegalArgumentException("Guest with email " + dto.getEmail() + " already exists");
            }
            guest.setEmail(dto.getEmail());
        }

        guestRepository.save(guest);
        return GuestMapper.toResponseDTO(guest);
    }

    @Transactional
    public void delete(Long id) {
        if (!guestRepository.existsById(id)) {
            throw new NoSuchElementException("Guest not found with id:  " + id);
        }
        guestRepository.deleteById(id);
    }
}