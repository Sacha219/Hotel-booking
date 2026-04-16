package com.example.hotelbooking.service;

import com.example.hotelbooking.dto.RoomRequestDTO;
import com.example.hotelbooking.dto.RoomResponseDTO;
import com.example.hotelbooking.entity.Hotel;
import com.example.hotelbooking.entity.Room;
import com.example.hotelbooking.repository.HotelRepository;
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
class RoomServiceTest {

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private HotelRepository hotelRepository;

    @InjectMocks
    private RoomService roomService;

    private Room room;
    private Hotel hotel;
    private RoomRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        hotel = new Hotel();
        hotel.setId(1L);
        hotel.setName("Test Hotel");

        room = new Room();
        room.setId(1L);
        room.setNumber("101");
        room.setPrice(100.0);
        room.setAvailable(true);
        room.setHotel(hotel);

        requestDTO = new RoomRequestDTO();
        requestDTO.setNumber("101");
        requestDTO.setPrice(100.0);
        requestDTO.setHotelId(1L);
        requestDTO.setAvailable(true);
    }

    @Test
    void findAll_ShouldReturnList() {
        when(roomRepository.findAll()).thenReturn(List.of(room));

        List<RoomResponseDTO> result = roomService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getNumber()).isEqualTo("101");
    }

    @Test
    void findById_ShouldReturnDTO_WhenExists() {
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));

        RoomResponseDTO result = roomService.findById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getNumber()).isEqualTo("101");
    }

    @Test
    void findById_ShouldThrow_WhenNotFound() {
        when(roomRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> roomService.findById(99L))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void findByHotelId_ShouldReturnList() {
        when(roomRepository.findByHotelId(1L)).thenReturn(List.of(room));

        List<RoomResponseDTO> result = roomService.findByHotelId(1L);

        assertThat(result).hasSize(1);
    }

    @Test
    void findAvailableRooms_ShouldReturnList() {
        LocalDate checkIn = LocalDate.now().plusDays(1);
        LocalDate checkOut = LocalDate.now().plusDays(3);
        when(roomRepository.findAvailableRooms(1L, checkIn, checkOut)).thenReturn(List.of(room));

        List<RoomResponseDTO> result = roomService.findAvailableRooms(1L, checkIn, checkOut);

        assertThat(result).hasSize(1);
    }

    @Test
    void create_ShouldSaveAndReturnDTO() {
        when(hotelRepository.findById(1L)).thenReturn(Optional.of(hotel));
        when(roomRepository.save(any(Room.class))).thenReturn(room);

        RoomResponseDTO result = roomService.create(requestDTO);

        assertThat(result).isNotNull();
        assertThat(result.getNumber()).isEqualTo("101");
        verify(roomRepository).save(any(Room.class));
    }

    @Test
    void create_ShouldThrow_WhenHotelNotFound() {
        when(hotelRepository.findById(99L)).thenReturn(Optional.empty());
        requestDTO.setHotelId(99L);

        assertThatThrownBy(() -> roomService.create(requestDTO))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void update_ShouldUpdateAndReturnDTO() {
        RoomRequestDTO updateDto = new RoomRequestDTO();
        updateDto.setNumber("202");
        updateDto.setPrice(150.0);
        updateDto.setAvailable(false);
        updateDto.setHotelId(1L);

        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(roomRepository.save(room)).thenReturn(room);

        RoomResponseDTO result = roomService.update(1L, updateDto);

        assertThat(result).isNotNull();
        assertThat(room.getNumber()).isEqualTo("202");
        assertThat(room.getPrice()).isEqualTo(150.0);
        assertThat(room.getAvailable()).isFalse();
        verify(roomRepository).save(room);
    }

    @Test
    void update_WhenAvailableIsNull_ShouldKeepOldAvailable() {

        RoomRequestDTO updateDto = new RoomRequestDTO();
        updateDto.setNumber("202");
        updateDto.setPrice(150.0);
        updateDto.setAvailable(null);
        updateDto.setHotelId(1L);

        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(roomRepository.save(room)).thenReturn(room);

        roomService.update(1L, updateDto);

        assertThat(room.getAvailable()).isTrue();
        verify(roomRepository).save(room);
    }

    @Test
    void delete_ShouldDelete_WhenExists() {
        when(roomRepository.existsById(1L)).thenReturn(true);
        doNothing().when(roomRepository).deleteById(1L);

        roomService.delete(1L);

        verify(roomRepository).deleteById(1L);
    }

    @Test
    void delete_ShouldThrow_WhenNotFound() {
        when(roomRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> roomService.delete(99L))
                .isInstanceOf(NoSuchElementException.class);
    }
}