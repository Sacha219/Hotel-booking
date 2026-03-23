package com.example.hotelbooking.mapper;

import com.example.hotelbooking.dto.HotelRequestDTO;
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
        dto.setStars(hotel.getStars());
        dto.setDescription(hotel.getDescription());
        dto.setAvailable(hotel.getAvailable());

        try {
            if (hotel.getRooms() != null && !hotel.getRooms().isEmpty()) {
                dto.setRoomIds(hotel.getRooms().stream()
                        .map(Room::getId)
                        .collect(Collectors.toList()));
            } else {
                dto.setRoomIds(new ArrayList<>());
            }
        } catch (Exception e) {
            dto.setRoomIds(new ArrayList<>());
        }

        if (hotel.getRooms() != null) {
            dto.setRoomIds(hotel.getRooms().stream().map(Room::getId).toList());
        }
        if (hotel.getAmenities() != null) {
            dto.setAmenityIds(hotel.getAmenities().stream().map(Amenity::getId).toList());
        }

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
        hotel.setStars(dto.getStars());
        hotel.setDescription(dto.getDescription());
        hotel.setAvailable(dto.getAvailable() == null || dto.getAvailable());

        return hotel;
    }
}