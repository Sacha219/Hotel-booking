package com.example.hotelbooking.service;

import com.example.hotelbooking.dto.BookingRequestDTO;
import com.example.hotelbooking.dto.BookingResponseDTO;
import com.example.hotelbooking.entity.Booking;
import com.example.hotelbooking.entity.Room;
import com.example.hotelbooking.entity.Guest;
import com.example.hotelbooking.mapper.BookingMapper;
import com.example.hotelbooking.repository.BookingRepository;
import com.example.hotelbooking.repository.RoomRepository;
import com.example.hotelbooking.repository.GuestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final GuestRepository guestRepository;

    @Transactional(readOnly = true)
    public List<BookingResponseDTO> findAll() {
        return bookingRepository.findAll().stream()
                .map(BookingMapper::toResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public BookingResponseDTO findById(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Booking not found with id: " + id));
        return BookingMapper.toResponseDTO(booking);
    }

    @Transactional(readOnly = true)
    public List<BookingResponseDTO> findByGuestId(Long guestId) {
        return bookingRepository.findByGuestId(guestId).stream()
                .map(BookingMapper::toResponseDTO)
                .toList();
    }

    @Transactional
    public BookingResponseDTO create(BookingRequestDTO dto) {
        Room room = roomRepository.findById(dto.getRoomId())
                .orElseThrow(() -> new NoSuchElementException("Room not found with id:" + dto.getRoomId()));

        Guest guest = guestRepository.findById(dto.getGuestId())
                .orElseThrow(() -> new NoSuchElementException("Guest not found with id: " + dto.getGuestId()));

        if (!dto.getCheckOutDate().isAfter(dto.getCheckInDate())) {
            throw new IllegalArgumentException("Дата выезда должна быть позже даты заезда");
        }

        if (!room.getAvailable()) {
            throw new IllegalStateException("Room not available for booking");
        }

        List<Booking> overlappingBookings = bookingRepository.findOverlappingBookings(
                dto.getCheckInDate(), dto.getCheckOutDate());

        boolean roomBooked = overlappingBookings.stream()
                .filter(b -> b.getStatus() == null || !"CANCELLED".equalsIgnoreCase(b.getStatus().trim()))
                .anyMatch(b -> b.getRoom().getId().equals(room.getId()));

        if (roomBooked) {
            throw new IllegalStateException("Room already booked for selected dates");
        }

        Booking booking = BookingMapper.toEntity(dto);
        booking.setRoom(room);
        booking.setGuest(guest);

        long nights = ChronoUnit.DAYS.between(dto.getCheckInDate(), dto.getCheckOutDate());
        double totalPrice = room.getPrice() * nights;
        booking.setTotalPrice(totalPrice);

        Booking savedBooking = bookingRepository.save(booking);
        return BookingMapper.toResponseDTO(savedBooking);
    }

    @Transactional
    public BookingResponseDTO updateStatus(Long id, String status) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Booking not found with id:  " + id));

        booking.setStatus(status);
        Booking updatedBooking = bookingRepository.save(booking);
        return BookingMapper.toResponseDTO(updatedBooking);
    }

    @Transactional
    public void cancel(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Booking not found  with id: " + id));

        booking.setStatus(" CANCELLED");
        bookingRepository.save(booking);
    }

    @Transactional
    public void deleteCancelled(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Booking not found with id: " + id));

        if (!"CANCELLED ".equalsIgnoreCase(booking.getStatus())) {
            throw new IllegalStateException("Удалить можно только отмененное бронирование");
        }

        bookingRepository.delete(booking);
    }
}
