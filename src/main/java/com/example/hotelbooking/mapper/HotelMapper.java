package com.example.hotelbooking.mapper;

import com.example.hotelbooking.dto.HotelResponseDTO;
import com.example.hotelbooking.entity.Hotel;
import com.example.hotelbooking.entity.Room;
import com.example.hotelbooking.entity.Amenity;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.stream.Collectors;

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

        if (hotel.getRooms() != null && !hotel.getRooms().isEmpty()) {
            dto.setRoomIds(hotel.getRooms().stream()
                    .map(Room::getId)
                    .collect(Collectors.toList()));
        } else {
            dto.setRoomIds(new ArrayList<>());
        }

        if (hotel.getAmenities() != null && !hotel.getAmenities().isEmpty()) {
            dto.setAmenityIds(hotel.getAmenities().stream()
                    .map(Amenity::getId)
                    .collect(Collectors.toList()));
        } else {
            dto.setAmenityIds(new ArrayList<>());
        }

        return dto;
    }
}