package com.example.hotelbooking.mapper;

import com.example.hotelbooking.dto.HotelRequestDTO;
import com.example.hotelbooking.dto.HotelResponseDTO;
import com.example.hotelbooking.entity.Hotel;
import org.springframework.stereotype.Component;

@Component
public class HotelMapper {

    public HotelResponseDTO toResponseDTO(Hotel hotel) {
        if (hotel == null) {
            return null;
        }

        HotelResponseDTO dto = new HotelResponseDTO();
        dto.setId(hotel.getId());
        dto.setName(hotel.getName());
        dto.setAddress(hotel.getAddress());
        dto.setCity(hotel.getCity());
        dto.setCountry(hotel.getCountry());
        dto.setStars(hotel.getStars());
        dto.setDescription(hotel.getDescription());
        dto.setPricePerNight(hotel.getPricePerNight());
        dto.setAvailable(hotel.getAvailable());

        return dto;
    }

    public Hotel toEntity(HotelRequestDTO dto) {
        if (dto == null) {
            return null;
        }

        Hotel hotel = new Hotel();
        hotel.setName(dto.getName());
        hotel.setAddress(dto.getAddress());
        hotel.setCity(dto.getCity());
        hotel.setCountry(dto.getCountry());
        hotel.setStars(dto.getStars());
        hotel.setDescription(dto.getDescription());
        hotel.setPricePerNight(dto.getPricePerNight());
        hotel.setAvailable(dto.getAvailable() != null ? dto.getAvailable() : true);

        return hotel;
    }
}