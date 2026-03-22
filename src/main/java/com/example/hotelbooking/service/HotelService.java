package com.example.hotelbooking.service;

import com.example.hotelbooking.dto.HotelRequestDTO;
import com.example.hotelbooking.dto.HotelResponseDTO;
import java.util.List;

public interface HotelService {
    HotelResponseDTO getHotelById(Long id);

    List<HotelResponseDTO> getAllHotels();

    List<HotelResponseDTO> getHotelsByCity(String city);

    List<HotelResponseDTO> getHotelsByStars(Integer stars);

    List<HotelResponseDTO> getHotelsByCityAndStars(String city, Integer stars);

    List<HotelResponseDTO> getAllHotelsPlain();

    List<HotelResponseDTO> getAllHotelsWithDetails();

    HotelResponseDTO updateHotel(Long id, HotelRequestDTO dto);

    void deleteHotel(Long id);

    HotelResponseDTO createHotel(HotelRequestDTO hotelRequestDTO);
}