package com.example.hotelbooking.service;

import com.example.hotelbooking.dto.HotelRequestDTO;
import com.example.hotelbooking.dto.HotelResponseDTO;
import java.util.List;
import org.springframework.data.domain.Page;

public interface HotelService {
    HotelResponseDTO getHotelById(Long id);

    List<HotelResponseDTO> getAllHotels();

    List<HotelResponseDTO> getHotelsByCity(String city);

    List<HotelResponseDTO> getHotelsByStars(Integer stars);

    List<HotelResponseDTO> getHotelsByCityAndStars(String city, Integer stars);

    void deleteHotel(Long id);

    HotelResponseDTO createHotel(HotelRequestDTO hotelRequestDTO);

    HotelResponseDTO updateHotel(Long id, HotelRequestDTO dto);

    Page<HotelResponseDTO> findHotelsByRoomTypeAndPrice(String roomType, Double minPrice, int page, int size);
}