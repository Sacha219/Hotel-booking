package com.example.hotelbooking.service;

import com.example.hotelbooking.dto.HotelRequestDTO;
import com.example.hotelbooking.dto.HotelResponseDTO;

import java.util.List;

public interface HotelService {

    HotelResponseDTO createHotel(HotelRequestDTO hotelRequestDTO);

    HotelResponseDTO getHotelById(Long id);

    List<HotelResponseDTO> getAllHotels();

    List<HotelResponseDTO> getHotelsByCity(String city);

    List<HotelResponseDTO> getHotelsByStars(Integer stars);

    List<HotelResponseDTO> getHotelsByCityAndStars(String city, Integer stars);

    HotelResponseDTO updateHotel(Long id, HotelRequestDTO hotelRequestDTO);

    void deleteHotel(Long id);
}