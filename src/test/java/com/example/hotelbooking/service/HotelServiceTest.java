package com.example.hotelbooking.service;

import com.example.hotelbooking.dto.HotelRequestDTO;
import com.example.hotelbooking.dto.HotelResponseDTO;
import com.example.hotelbooking.entity.Hotel;
import com.example.hotelbooking.mapper.HotelMapper;
import com.example.hotelbooking.repository.HotelRepository;
import com.example.hotelbooking.service.impl.HotelServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HotelServiceTest {

    @Mock
    private HotelRepository hotelRepository;

    @Mock
    private HotelMapper hotelMapper;

    @Mock
    private HotelCachingService hotelCachingService;

    @InjectMocks
    private HotelServiceImpl hotelService;

    private Hotel hotel;
    private HotelRequestDTO requestDTO;
    private HotelResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        hotel = new Hotel();
        hotel.setId(1L);
        hotel.setName("Test Hotel");
        hotel.setCity("Minsk");
        hotel.setStars(4);
        hotel.setAvailable(true);

        requestDTO = new HotelRequestDTO();
        requestDTO.setName("Test Hotel");
        requestDTO.setCity("Minsk");
        requestDTO.setStars(4);

        responseDTO = new HotelResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setName("Test Hotel");
        responseDTO.setCity("Minsk");
        responseDTO.setStars(4);
    }

    @Test
    void getHotelById_ShouldReturnResponseDTO_WhenExists() {
        when(hotelRepository.findById(1L)).thenReturn(Optional.of(hotel));
        when(hotelMapper.toResponseDTO(hotel)).thenReturn(responseDTO);

        HotelResponseDTO result = hotelService.getHotelById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void getHotelById_ShouldThrowException_WhenNotFound() {
        when(hotelRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> hotelService.getHotelById(99L))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void getAllHotels_ShouldReturnList() {
        when(hotelRepository.findAll()).thenReturn(List.of(hotel));
        when(hotelMapper.toResponseDTO(hotel)).thenReturn(responseDTO);

        List<HotelResponseDTO> result = hotelService.getAllHotels();

        assertThat(result).hasSize(1);
    }

    @Test
    void getHotelsByCity_ShouldReturnFilteredList() {
        when(hotelRepository.findByCityIgnoreCase("Minsk")).thenReturn(List.of(hotel));
        when(hotelMapper.toResponseDTO(hotel)).thenReturn(responseDTO);

        List<HotelResponseDTO> result = hotelService.getHotelsByCity("Minsk");

        assertThat(result).hasSize(1);
    }

    @Test
    void getHotelsByStars_ShouldReturnFilteredList() {
        when(hotelRepository.findByStars(4)).thenReturn(List.of(hotel));
        when(hotelMapper.toResponseDTO(hotel)).thenReturn(responseDTO);

        List<HotelResponseDTO> result = hotelService.getHotelsByStars(4);

        assertThat(result).hasSize(1);
    }

    @Test
    void getHotelsByCityAndStars_ShouldReturnFilteredList() {
        when(hotelRepository.findByCityIgnoreCaseAndStars("Minsk", 4)).thenReturn(List.of(hotel));
        when(hotelMapper.toResponseDTO(hotel)).thenReturn(responseDTO);

        List<HotelResponseDTO> result = hotelService.getHotelsByCityAndStars("Minsk", 4);

        assertThat(result).hasSize(1);
    }

    @Test
    void createHotel_ShouldSaveAndInvalidateCache() {
        when(hotelMapper.toEntity(requestDTO)).thenReturn(hotel);
        when(hotelRepository.save(hotel)).thenReturn(hotel);
        when(hotelMapper.toResponseDTO(hotel)).thenReturn(responseDTO);

        HotelResponseDTO result = hotelService.createHotel(requestDTO);

        assertThat(result).isNotNull();
        verify(hotelCachingService).invalidateAll();
    }

    @Test
    void updateHotel_ShouldUpdateAndInvalidateCache() {
        HotelRequestDTO updateDto = new HotelRequestDTO();
        updateDto.setName("Updated Name");
        updateDto.setCity("Minsk");
        updateDto.setStars(5);
        updateDto.setAvailable(true);

        when(hotelRepository.findById(1L)).thenReturn(Optional.of(hotel));
        when(hotelRepository.save(any(Hotel.class))).thenReturn(hotel);
        when(hotelMapper.toResponseDTO(hotel)).thenReturn(responseDTO);

        HotelResponseDTO result = hotelService.updateHotel(1L, updateDto);

        assertThat(result).isNotNull();
        verify(hotelCachingService).invalidateByHotelId(1L);
        assertThat(hotel.getName()).isEqualTo("Updated Name");
    }

    @Test
    void deleteHotel_ShouldDeleteAndInvalidateCache() {
        when(hotelRepository.existsById(1L)).thenReturn(true);
        doNothing().when(hotelRepository).deleteById(1L);

        hotelService.deleteHotel(1L);

        verify(hotelCachingService).invalidateByHotelId(1L);
        verify(hotelRepository).deleteById(1L);
    }

    @Test
    void createHotelsBulk_ShouldSaveAll() {
        List<HotelRequestDTO> requests = List.of(requestDTO);
        when(hotelMapper.toEntity(requestDTO)).thenReturn(hotel);
        when(hotelRepository.findByNameIgnoreCase("Test Hotel")).thenReturn(Optional.empty());
        when(hotelRepository.saveAll(anyList())).thenReturn(List.of(hotel));
        when(hotelMapper.toResponseDTO(hotel)).thenReturn(responseDTO);

        List<HotelResponseDTO> result = hotelService.createHotelsBulk(requests);

        assertThat(result).hasSize(1);
        verify(hotelRepository).saveAll(anyList());
    }

    @Test
    void createHotelsBulk_ShouldThrow_WhenDuplicateInRequest() {
        List<HotelRequestDTO> requests = List.of(requestDTO, requestDTO);

        assertThatThrownBy(() -> hotelService.createHotelsBulk(requests))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void createHotelsBulk_ShouldThrow_WhenNameExistsInDb() {
        when(hotelRepository.findByNameIgnoreCase("Test Hotel")).thenReturn(Optional.of(hotel));
        List<HotelRequestDTO> requests = List.of(requestDTO);

        assertThatThrownBy(() -> hotelService.createHotelsBulk(requests))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void createHotelsBulkWithoutTransaction_ShouldSaveOneByOneAndThrowOnDuplicate() {
        HotelRequestDTO request1 = new HotelRequestDTO();
        request1.setName("First");
        HotelRequestDTO request2 = new HotelRequestDTO();
        request2.setName("Second");

        Hotel hotel1 = new Hotel();
        hotel1.setId(1L);

        when(hotelRepository.findByNameIgnoreCase("First")).thenReturn(Optional.empty());
        when(hotelMapper.toEntity(request1)).thenReturn(hotel1);
        when(hotelRepository.save(hotel1)).thenReturn(hotel1);
        when(hotelMapper.toResponseDTO(hotel1)).thenReturn(new HotelResponseDTO());

        when(hotelRepository.findByNameIgnoreCase("Second")).thenReturn(Optional.of(new Hotel()));

        assertThatThrownBy(() -> hotelService.createHotelsBulkWithoutTransaction(List.of(request1, request2)));
        verify(hotelCachingService, never()).invalidateAll();

        verify(hotelRepository, times(1)).save(hotel1);
        verify(hotelCachingService, never()).invalidateAll();
    }

    @Test
    void createHotelsBulkWithoutTransaction_ShouldSaveAllAndInvalidateCache_WhenNoDuplicates() {
        HotelRequestDTO request1 = new HotelRequestDTO();
        request1.setName("First");
        HotelRequestDTO request2 = new HotelRequestDTO();
        request2.setName("Second");

        Hotel hotel1 = new Hotel();
        hotel1.setId(1L);
        Hotel hotel2 = new Hotel();
        hotel2.setId(2L);

        when(hotelRepository.findByNameIgnoreCase("First")).thenReturn(Optional.empty());
        when(hotelRepository.findByNameIgnoreCase("Second")).thenReturn(Optional.empty());
        when(hotelMapper.toEntity(request1)).thenReturn(hotel1);
        when(hotelMapper.toEntity(request2)).thenReturn(hotel2);
        when(hotelRepository.save(hotel1)).thenReturn(hotel1);
        when(hotelRepository.save(hotel2)).thenReturn(hotel2);
        when(hotelMapper.toResponseDTO(hotel1)).thenReturn(new HotelResponseDTO());
        when(hotelMapper.toResponseDTO(hotel2)).thenReturn(new HotelResponseDTO());

        List<HotelResponseDTO> result = hotelService.createHotelsBulkWithoutTransaction(List.of(request1, request2));

        assertThat(result).hasSize(2);
        verify(hotelRepository, times(2)).save(any(Hotel.class));
        verify(hotelCachingService).invalidateAll();
    }

    @Test
    void findHotelsByRoomTypeAndPrice_ShouldDelegateToCachingService() {
        String roomType = "DELUXE";
        Double minPrice = 100.0;
        int page = 0;
        int size = 10;
        Page<HotelResponseDTO> expectedPage = new PageImpl<>(List.of(responseDTO));
        when(hotelCachingService.findHotelsByRoomTypeAndPriceCached(roomType, minPrice, page, size))
                .thenReturn(expectedPage);

        Page<HotelResponseDTO> result = hotelService.findHotelsByRoomTypeAndPrice(roomType, minPrice, page, size);

        assertThat(result).isSameAs(expectedPage);
        verify(hotelCachingService).findHotelsByRoomTypeAndPriceCached(roomType, minPrice, page, size);
    }

    @Test
    void updateHotel_ShouldSetAvailableFromDto_WhenDtoHasAvailable() {
        HotelRequestDTO updateDto = new HotelRequestDTO();
        updateDto.setName("Updated Hotel");
        updateDto.setCity("Minsk");
        updateDto.setStars(5);
        updateDto.setPricePerNight(200.0);
        updateDto.setAvailable(false);

        when(hotelRepository.findById(1L)).thenReturn(Optional.of(hotel));
        when(hotelRepository.save(any(Hotel.class))).thenReturn(hotel);
        when(hotelMapper.toResponseDTO(hotel)).thenReturn(responseDTO);

        hotelService.updateHotel(1L, updateDto);

        assertThat(hotel.getAvailable()).isFalse();
        verify(hotelRepository).save(hotel);
    }

    @Test
    void updateHotel_ShouldKeepExistingAvailable_WhenDtoHasNoAvailable() {
        HotelRequestDTO updateDto = new HotelRequestDTO();
        updateDto.setName("Updated Hotel");
        updateDto.setCity("Minsk");
        updateDto.setStars(5);
        updateDto.setPricePerNight(200.0);
        updateDto.setAvailable(null);

        hotel.setAvailable(true);

        when(hotelRepository.findById(1L)).thenReturn(Optional.of(hotel));
        when(hotelRepository.save(any(Hotel.class))).thenReturn(hotel);
        when(hotelMapper.toResponseDTO(hotel)).thenReturn(responseDTO);

        hotelService.updateHotel(1L, updateDto);

        assertThat(hotel.getAvailable()).isTrue();
        verify(hotelRepository).save(hotel);
    }

    @Test
    void deleteHotel_ShouldThrowException_WhenHotelNotFound() {
        when(hotelRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> hotelService.deleteHotel(99L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("99");
        verify(hotelRepository, never()).deleteById(anyLong());
    }

    @Test
    void deleteHotel_ShouldDelete_WhenHotelExists() {
        when(hotelRepository.existsById(1L)).thenReturn(true);
        doNothing().when(hotelRepository).deleteById(1L);

        hotelService.deleteHotel(1L);

        verify(hotelRepository).deleteById(1L);
        verify(hotelCachingService).invalidateByHotelId(1L);
    }
}