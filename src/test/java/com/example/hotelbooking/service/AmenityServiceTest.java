package com.example.hotelbooking.service;

import com.example.hotelbooking.dto.AmenityDTO;
import com.example.hotelbooking.entity.Amenity;
import com.example.hotelbooking.entity.Room;
import com.example.hotelbooking.repository.AmenityRepository;
import com.example.hotelbooking.repository.HotelRepository;
import com.example.hotelbooking.repository.RoomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AmenityServiceTest {

    @Mock
    private AmenityRepository amenityRepository;

    @Mock
    private HotelRepository hotelRepository;

    @Mock
    private RoomRepository roomRepository;

    @InjectMocks
    private AmenityService amenityService;

    private Amenity amenity;
    private AmenityDTO dto;

    @BeforeEach
    void setUp() {
        amenity = new Amenity();
        amenity.setId(1L);
        amenity.setName("WiFi");
        amenity.setDescription("Fast internet");
        amenity.setIcon("wifi");
        amenity.setRooms(new HashSet<>());

        dto = new AmenityDTO();
        dto.setId(1L);
        dto.setName("WiFi");
        dto.setDescription("Fast internet");
        dto.setIcon("wifi");
    }

    @Test
    void findAll_ShouldReturnList() {
        when(amenityRepository.findAll()).thenReturn(List.of(amenity));

        List<AmenityDTO> result = amenityService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("WiFi");
    }

    @Test
    void findById_ShouldReturnDTO_WhenExists() {
        when(amenityRepository.findById(1L)).thenReturn(Optional.of(amenity));

        AmenityDTO result = amenityService.findById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("WiFi");
    }

    @Test
    void findById_ShouldThrow_WhenNotFound() {
        when(amenityRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> amenityService.findById(99L))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void findByName_ShouldReturnDTO_WhenExists() {
        when(amenityRepository.findByNameIgnoreCase("WiFi")).thenReturn(Optional.of(amenity));

        AmenityDTO result = amenityService.findByName("WiFi");

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("WiFi");
    }

    @Test
    void create_ShouldSaveAndReturnDTO() {
        when(amenityRepository.existsByNameIgnoreCase("WiFi")).thenReturn(false);
        when(amenityRepository.save(any(Amenity.class))).thenReturn(amenity);

        AmenityDTO result = amenityService.create(dto);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("WiFi");
        verify(amenityRepository).save(any(Amenity.class));
    }

    @Test
    void create_ShouldThrow_WhenNameExists() {
        when(amenityRepository.existsByNameIgnoreCase("WiFi")).thenReturn(true);

        assertThatThrownBy(() -> amenityService.create(dto))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void update_ShouldUpdateAndReturnDTO() {
        AmenityDTO updateDto = new AmenityDTO();
        updateDto.setName("Updated WiFi");
        updateDto.setDescription("Updated desc");
        updateDto.setIcon("new-icon");

        when(amenityRepository.findById(1L)).thenReturn(Optional.of(amenity));
        when(amenityRepository.save(amenity)).thenReturn(amenity);

        AmenityDTO result = amenityService.update(1L, updateDto);

        assertThat(result).isNotNull();
        assertThat(amenity.getName()).isEqualTo("Updated WiFi");
        verify(amenityRepository).save(amenity);
    }

    @Test
    void delete_ShouldRemoveRelationsAndDelete() {
        Room room = new Room();
        room.setId(1L);
        room.setAmenities(new HashSet<>());
        room.getAmenities().add(amenity);
        amenity.getRooms().add(room);

        when(amenityRepository.findById(1L)).thenReturn(Optional.of(amenity));
        doNothing().when(amenityRepository).delete(amenity);

        amenityService.delete(1L);

        assertThat(room.getAmenities()).isEmpty();
        assertThat(amenity.getRooms()).isEmpty();
        verify(amenityRepository).delete(amenity);
    }

    @Test
    void delete_ShouldThrow_WhenNotFound() {
        when(amenityRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> amenityService.delete(99L))
                .isInstanceOf(NoSuchElementException.class);
    }
}
