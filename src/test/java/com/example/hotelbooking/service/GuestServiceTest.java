package com.example.hotelbooking.service;

import com.example.hotelbooking.dto.*;
import com.example.hotelbooking.entity.Booking;
import com.example.hotelbooking.entity.Guest;
import com.example.hotelbooking.entity.Room;
import com.example.hotelbooking.repository.BookingRepository;
import com.example.hotelbooking.repository.GuestRepository;
import com.example.hotelbooking.repository.RoomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GuestServiceTest {

    @Mock
    private GuestRepository guestRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private RoomRepository roomRepository;

    @InjectMocks
    private GuestService guestService;

    private Guest guest;
    private GuestRequestDTO requestDTO;
    private GuestResponseDTO responseDTO;
    private Room room;

    @BeforeEach
    void setUp() {
        guest = new Guest();
        guest.setId(1L);
        guest.setFirstName("John");
        guest.setLastName("Doe");
        guest.setEmail("john@example.com");
        guest.setPhone("123456789");

        requestDTO = new GuestRequestDTO();
        requestDTO.setFirstName("John");
        requestDTO.setLastName("Doe");
        requestDTO.setEmail("john@example.com");
        requestDTO.setPhone("123456789");

        responseDTO = new GuestResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setFirstName("John");
        responseDTO.setLastName("Doe");
        responseDTO.setEmail("john@example.com");

        room = new Room();
        room.setId(1L);
        room.setPrice(100.0);
    }

    @Test
    void findAll_ShouldReturnList() {
        when(guestRepository.findAll()).thenReturn(List.of(guest));

        List<GuestResponseDTO> result = guestService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEmail()).isEqualTo("john@example.com");
    }

    @Test
    void findById_ShouldReturnDTO_WhenExists() {
        when(guestRepository.findById(1L)).thenReturn(Optional.of(guest));

        GuestResponseDTO result = guestService.findById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("john@example.com");
    }

    @Test
    void findById_ShouldThrow_WhenNotFound() {
        when(guestRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> guestService.findById(99L))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void findByEmail_ShouldReturnDTO_WhenExists() {
        when(guestRepository.findByEmailIgnoreCase("john@example.com")).thenReturn(Optional.of(guest));

        GuestResponseDTO result = guestService.findByEmail("john@example.com");

        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("john@example.com");
    }

    @Test
    void create_ShouldSaveAndReturnDTO() {
        when(guestRepository.save(any(Guest.class))).thenReturn(guest);

        GuestResponseDTO result = guestService.create(requestDTO);

        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("john@example.com");
        verify(guestRepository).save(any(Guest.class));
    }

    @Test
    void create_ShouldThrowDataIntegrityViolation_WhenDuplicateEmail() {
        when(guestRepository.save(any(Guest.class))).thenThrow(new DataIntegrityViolationException("duplicate"));

        assertThatThrownBy(() -> guestService.create(requestDTO))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void update_ShouldUpdateAndReturnDTO() {
        GuestRequestDTO updateDto = new GuestRequestDTO();
        updateDto.setFirstName("Jane");
        updateDto.setLastName("Smith");
        updateDto.setEmail("jane@example.com");
        updateDto.setPhone("987654321");

        when(guestRepository.findById(1L)).thenReturn(Optional.of(guest));
        when(guestRepository.existsByEmailIgnoreCase("jane@example.com")).thenReturn(false);
        when(guestRepository.save(guest)).thenReturn(guest);

        GuestResponseDTO result = guestService.update(1L, updateDto);

        assertThat(result).isNotNull();
        assertThat(guest.getFirstName()).isEqualTo("Jane");
        assertThat(guest.getEmail()).isEqualTo("jane@example.com");
        verify(guestRepository).save(guest);
    }

    @Test
    void update_ShouldThrow_WhenEmailAlreadyExists() {
        GuestRequestDTO updateDto = new GuestRequestDTO();
        updateDto.setEmail("existing@example.com");

        when(guestRepository.findById(1L)).thenReturn(Optional.of(guest));
        when(guestRepository.existsByEmailIgnoreCase("existing@example.com")).thenReturn(true);

        assertThatThrownBy(() -> guestService.update(1L, updateDto))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void delete_ShouldDelete_WhenExists() {
        when(guestRepository.existsById(1L)).thenReturn(true);
        doNothing().when(guestRepository).deleteById(1L);

        guestService.delete(1L);

        verify(guestRepository).deleteById(1L);
    }

    @Test
    void delete_ShouldThrow_WhenNotFound() {
        when(guestRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> guestService.delete(99L))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void createGuestWithBookings_ShouldCreateGuestAndBookings() {
        BookingRequestDTO bookingDto = new BookingRequestDTO();
        bookingDto.setRoomId(1L);
        bookingDto.setCheckInDate(LocalDate.now().plusDays(1));
        bookingDto.setCheckOutDate(LocalDate.now().plusDays(3));
        bookingDto.setStatus("PENDING");

        GuestWithBookingsDTO dto = new GuestWithBookingsDTO();
        dto.setFirstName("John");
        dto.setLastName("Doe");
        dto.setEmail("john@example.com");
        dto.setPhone("123");
        dto.setBookings(List.of(bookingDto));

        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(guestRepository.save(any(Guest.class))).thenReturn(guest);
        when(bookingRepository.saveAll(anyList())).thenReturn(List.of(new Booking()));

        GuestResponseDTO result = guestService.createGuestWithBookings(dto);

        assertThat(result).isNotNull();
        verify(guestRepository).save(any(Guest.class));
        verify(bookingRepository).saveAll(anyList());
    }
}