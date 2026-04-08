package com.example.hotelbooking.service;

import com.example.hotelbooking.dto.BookingRequestDTO;
import com.example.hotelbooking.dto.BookingResponseDTO;
import com.example.hotelbooking.entity.Booking;
import com.example.hotelbooking.entity.Guest;
import com.example.hotelbooking.entity.Room;
import com.example.hotelbooking.mapper.BookingMapper;
import com.example.hotelbooking.repository.BookingRepository;
import com.example.hotelbooking.repository.GuestRepository;
import com.example.hotelbooking.repository.RoomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private GuestRepository guestRepository;

    @InjectMocks
    private BookingService bookingService;

    private Room room;
    private Guest guest;
    private Booking booking;
    private BookingRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        room = new Room();
        room.setId(1L);
        room.setPrice(100.0);
        room.setAvailable(true);

        guest = new Guest();
        guest.setId(1L);
        guest.setEmail("guest@example.com");

        booking = new Booking();
        booking.setId(1L);
        booking.setRoom(room);
        booking.setGuest(guest);
        booking.setCheckInDate(LocalDate.now().plusDays(1));
        booking.setCheckOutDate(LocalDate.now().plusDays(3));
        booking.setStatus("CONFIRMED");
        booking.setTotalPrice(200.0);

        requestDTO = new BookingRequestDTO();
        requestDTO.setRoomId(1L);
        requestDTO.setGuestId(1L);
        requestDTO.setCheckInDate(LocalDate.now().plusDays(1));
        requestDTO.setCheckOutDate(LocalDate.now().plusDays(3));
        requestDTO.setStatus("CONFIRMED");
    }

    @Test
    void findAll_ShouldReturnList() {
        when(bookingRepository.findAll()).thenReturn(List.of(booking));

        List<BookingResponseDTO> result = bookingService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo("CONFIRMED");
    }

    @Test
    void findById_ShouldReturnDTO_WhenExists() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        BookingResponseDTO result = bookingService.findById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo("CONFIRMED");
    }

    @Test
    void findById_ShouldThrow_WhenNotFound() {
        when(bookingRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.findById(99L))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void findByGuestId_ShouldReturnList() {
        when(bookingRepository.findByGuestId(1L)).thenReturn(List.of(booking));

        List<BookingResponseDTO> result = bookingService.findByGuestId(1L);

        assertThat(result).hasSize(1);
    }

    @Test
    void create_ShouldSaveBooking_WhenValid() {
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(guestRepository.findById(1L)).thenReturn(Optional.of(guest));
        when(bookingRepository.findOverlappingBookings(any(), any())).thenReturn(List.of());
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        BookingResponseDTO result = bookingService.create(requestDTO);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo("CONFIRMED");
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    void create_ShouldThrow_WhenRoomNotAvailable() {
        room.setAvailable(false);
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(guestRepository.findById(1L)).thenReturn(Optional.of(guest));

        assertThatThrownBy(() -> bookingService.create(requestDTO))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not available");
    }

    @Test
    void create_ShouldThrow_WhenRoomAlreadyBooked() {
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(guestRepository.findById(1L)).thenReturn(Optional.of(guest));
        when(bookingRepository.findOverlappingBookings(any(), any()))
                .thenReturn(List.of(booking));

        assertThatThrownBy(() -> bookingService.create(requestDTO))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already booked");
    }

    @Test
    void updateStatus_ShouldUpdateAndReturnDTO() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(booking)).thenReturn(booking);

        BookingResponseDTO result = bookingService.updateStatus(1L, "CANCELLED");

        assertThat(result).isNotNull();
        assertThat(booking.getStatus()).isEqualTo("CANCELLED");
        verify(bookingRepository).save(booking);
    }

    @Test
    void cancel_ShouldSetStatusCancelled() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(booking)).thenReturn(booking);

        bookingService.cancel(1L);

        assertThat(booking.getStatus()).isEqualTo("CANCELLED");
        verify(bookingRepository).save(booking);
    }
}