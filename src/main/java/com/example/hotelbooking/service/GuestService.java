package com.example.hotelbooking.service;

import com.example.hotelbooking.dto.BookingRequestDTO;
import com.example.hotelbooking.dto.GuestWithBookingsDTO;
import com.example.hotelbooking.dto.GuestRequestDTO;
import com.example.hotelbooking.dto.GuestResponseDTO;
import com.example.hotelbooking.entity.Booking;
import com.example.hotelbooking.entity.Guest;
import com.example.hotelbooking.entity.Room;
import com.example.hotelbooking.exception.TransactionDemoException;
import com.example.hotelbooking.mapper.GuestMapper;
import com.example.hotelbooking.repository.BookingRepository;
import com.example.hotelbooking.repository.GuestRepository;
import com.example.hotelbooking.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Slf4j
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
        Guest savedGuest = guestRepository.save(guest);
        return GuestMapper.toResponseDTO(savedGuest);
    }

    @Transactional
    public GuestResponseDTO update(Long id, GuestRequestDTO dto) {
        Guest guest = guestRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Guest not found with id: " + id));

        guest.setFirstName(dto.getFirstName());
        guest.setLastName(dto.getLastName());
        guest.setPhone(dto.getPhone());

        if (!guest.getEmail().equalsIgnoreCase(dto.getEmail())) {
            if (guestRepository.existsByEmailIgnoreCase(dto.getEmail())) {
                throw new IllegalArgumentException("Guest with email " + dto.getEmail() + " already exists");
            }
            guest.setEmail(dto.getEmail());
        }

        Guest updatedGuest = guestRepository.save(guest);
        return GuestMapper.toResponseDTO(updatedGuest);
    }

    @Transactional
    public void delete(Long id) {
        if (!guestRepository.existsById(id)) {
            throw new NoSuchElementException("Guest not found with id: " + id);
        }
        guestRepository.deleteById(id);
    }

    private Guest createGuestEntity(GuestWithBookingsDTO dto) {
        Guest guest = new Guest();
        guest.setFirstName(dto.getFirstName());
        guest.setLastName(dto.getLastName());
        guest.setEmail(dto.getEmail());
        guest.setPhone(dto.getPhone());
        guest.setRegistrationDate(LocalDate.now());
        return guestRepository.save(guest);
    }

    private List<Booking> createBookings(Guest guest, GuestWithBookingsDTO dto) {
        List<Booking> bookings = new ArrayList<>();
        for (BookingRequestDTO bookingDTO : dto.getBookings()) {
            Room room = roomRepository.findById(bookingDTO.getRoomId())
                    .orElseThrow(() -> new RuntimeException("Room not found"));

            Booking booking = new Booking();
            booking.setCheckInDate(bookingDTO.getCheckInDate());
            booking.setCheckOutDate(bookingDTO.getCheckOutDate());
            booking.setStatus(bookingDTO.getStatus() != null ? bookingDTO.getStatus() : "PENDING");
            booking.setGuest(guest);
            booking.setRoom(room);

            long nights = ChronoUnit.DAYS.between(bookingDTO.getCheckInDate(), bookingDTO.getCheckOutDate());
            booking.setTotalPrice(room.getPrice() * nights);

            bookings.add(booking);
        }
        return bookingRepository.saveAll(bookings);
    }

    @Transactional
    public GuestResponseDTO createGuestWithBookings(GuestWithBookingsDTO dto) {
        Guest guest = createGuestEntity(dto);
        List<Booking> bookings = createBookings(guest, dto);
        guest.getBookings().addAll(bookings);
        log.info("Гость с ID {} и бронированиями успешно создан", guest.getId());
        return GuestMapper.toResponseDTO(guest);
    }

    public void createGuestWithBookingsWithoutTx(GuestWithBookingsDTO dto) {
        Guest guest = createGuestEntity(dto);
        List<Booking> bookings = createBookings(guest, dto);
        guest.getBookings().addAll(bookings);
        throw new TransactionDemoException("Ошибка после сохранения гостя и бронирований (БЕЗ @Transactional)");
    }
}