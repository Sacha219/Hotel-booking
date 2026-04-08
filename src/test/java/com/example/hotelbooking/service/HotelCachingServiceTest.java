package com.example.hotelbooking.service;

import com.example.hotelbooking.cache.HotelCache;
import com.example.hotelbooking.cache.HotelSearchKey;
import com.example.hotelbooking.dto.HotelResponseDTO;
import com.example.hotelbooking.entity.Hotel;
import com.example.hotelbooking.mapper.HotelMapper;
import com.example.hotelbooking.repository.HotelRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HotelCachingServiceTest {

    @Mock
    private HotelRepository hotelRepository;

    @Mock
    private HotelMapper hotelMapper;

    @Mock
    private HotelCache hotelCache;

    @InjectMocks
    private HotelCachingService cachingService;

    private Hotel hotel;
    private HotelResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        cachingService.init();

        hotel = new Hotel();
        hotel.setId(1L);
        hotel.setName("Test Hotel");

        responseDTO = new HotelResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setName("Test Hotel");
    }

    @Test
    void findHotelsByRoomTypeAndPrice_ShouldCallRepositoryAndMapResult() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("name").ascending());
        Page<Hotel> hotelPage = new PageImpl<>(List.of(hotel), pageable, 1);
        when(hotelRepository.findHotelsByRoomTypeAndPrice(eq("DELUXE"), eq(100.0), any(Pageable.class)))
                .thenReturn(hotelPage);
        when(hotelMapper.toResponseDTO(hotel)).thenReturn(responseDTO);

        Page<HotelResponseDTO> result = cachingService.findHotelsByRoomTypeAndPrice("DELUXE", 100.0, 0, 10);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo(1L);
        verify(hotelRepository).findHotelsByRoomTypeAndPrice(eq("DELUXE"), eq(100.0), any(Pageable.class));
        verify(hotelMapper).toResponseDTO(hotel);
    }

    @Test
    void findHotelsByRoomTypeAndPriceCached_ShouldReturnFromCache_WhenHit() {
        HotelSearchKey key = new HotelSearchKey("DELUXE", 100.0, null, 0, 10, "name");
        Page<HotelResponseDTO> cachedPage = new PageImpl<>(List.of(responseDTO));
        when(hotelCache.get(key)).thenReturn(cachedPage);

        Page<HotelResponseDTO> result = cachingService.findHotelsByRoomTypeAndPriceCached("DELUXE", 100.0, 0, 10);

        assertThat(result).isSameAs(cachedPage);
        verify(hotelCache, times(1)).get(key);
        verify(hotelRepository, never()).findHotelsByRoomTypeAndPrice(any(), any(), any());
    }

    @Test
    void findHotelsByRoomTypeAndPriceCached_ShouldFetchAndCache_WhenMiss() {
        HotelSearchKey key = new HotelSearchKey("DELUXE", 100.0, null, 0, 10, "name");
        when(hotelCache.get(key)).thenReturn(null);
        Pageable pageable = PageRequest.of(0, 10, Sort.by("name").ascending());
        Page<Hotel> hotelPage = new PageImpl<>(List.of(hotel), pageable, 1);
        when(hotelRepository.findHotelsByRoomTypeAndPrice(eq("DELUXE"), eq(100.0), any(Pageable.class)))
                .thenReturn(hotelPage);
        when(hotelMapper.toResponseDTO(hotel)).thenReturn(responseDTO);

        Page<HotelResponseDTO> result = cachingService.findHotelsByRoomTypeAndPriceCached("DELUXE", 100.0, 0, 10);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(hotelCache).put(eq(key), any(Page.class));
    }

    @Test
    void findHotelsByRoomTypeAndPriceNative_ShouldReturnFromCache_WhenHit() {
        HotelSearchKey key = new HotelSearchKey("DELUXE", 100.0, null, 0, 10, "name-native");
        Page<HotelResponseDTO> cachedPage = new PageImpl<>(List.of(responseDTO));
        when(hotelCache.get(key)).thenReturn(cachedPage);

        Page<HotelResponseDTO> result = cachingService.findHotelsByRoomTypeAndPriceNative("DELUXE", 100.0, 0, 10);

        assertThat(result).isSameAs(cachedPage);
        verify(hotelCache).get(key);
        verify(hotelRepository, never()).findHotelIdsByRoomTypeAndPriceNative(any(), any(), any());
    }

    @Test
    void findHotelsByRoomTypeAndPriceNative_ShouldFetchAndCache_WhenMissAndIdsNotEmpty() {
        HotelSearchKey key = new HotelSearchKey("DELUXE", 100.0, null, 0, 10, "name-native");
        when(hotelCache.get(key)).thenReturn(null);
        Pageable pageable = PageRequest.of(0, 10, Sort.by("name").ascending());
        Page<Long> idsPage = new PageImpl<>(List.of(1L, 2L), pageable, 2);
        when(hotelRepository.findHotelIdsByRoomTypeAndPriceNative("DELUXE", 100.0, pageable))
                .thenReturn(idsPage);

        Hotel hotel1 = new Hotel();
        hotel1.setId(1L);
        hotel1.setName("Hotel 1");
        Hotel hotel2 = new Hotel();
        hotel2.setId(2L);
        hotel2.setName("Hotel 2");
        List<Hotel> hotels = List.of(hotel1, hotel2);
        when(hotelRepository.findAllWithDetailsByIds(List.of(1L, 2L))).thenReturn(hotels);
        when(hotelMapper.toResponseDTO(any(Hotel.class))).thenReturn(new HotelResponseDTO());

        Page<HotelResponseDTO> result = cachingService.findHotelsByRoomTypeAndPriceNative("DELUXE", 100.0, 0, 10);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        verify(hotelCache).put(eq(key), any(Page.class));
    }

    @Test
    void findHotelsByRoomTypeAndPriceNative_ShouldReturnEmptyPage_WhenIdsPageEmpty() {
        HotelSearchKey key = new HotelSearchKey("DELUXE", 100.0, null, 0, 10, "name-native");
        when(hotelCache.get(key)).thenReturn(null);
        Pageable pageable = PageRequest.of(0, 10, Sort.by("name").ascending());
        Page<Long> emptyIdsPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
        when(hotelRepository.findHotelIdsByRoomTypeAndPriceNative("DELUXE", 100.0, pageable))
                .thenReturn(emptyIdsPage);

        Page<HotelResponseDTO> result = cachingService.findHotelsByRoomTypeAndPriceNative("DELUXE", 100.0, 0, 10);

        assertThat(result).isEmpty();
        verify(hotelCache).put(eq(key), any(Page.class));
        verify(hotelRepository, never()).findAllWithDetailsByIds(any());
    }

    @Test
    void invalidateByHotelId_ShouldClearCacheByHotelId() {
        cachingService.invalidateByHotelId(1L);
        verify(hotelCache).clearByHotelId(1L);
    }

    @Test
    void invalidateAll_ShouldClearAllCache() {
        cachingService.invalidateAll();
        verify(hotelCache).clearAll();
    }
}