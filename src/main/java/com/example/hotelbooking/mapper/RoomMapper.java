package com.example.hotelbooking.mapper;

import com.example.hotelbooking.dto.RoomRequestDTO;
import com.example.hotelbooking.dto.RoomResponseDTO;
import com.example.hotelbooking.entity.Room;
import com.example.hotelbooking.entity.Amenity;
import java.util.stream.Collectors;

public class RoomMapper {

    private RoomMapper() {

    }

    public static RoomResponseDTO toResponseDTO(Room room) {
        if (room == null) {
            return null;
        }

        RoomResponseDTO dto = new RoomResponseDTO();
        dto.setId(room.getId());
        dto.setRoomNumber(room.getRoomNumber());
        dto.setFloor(room.getFloor());
        dto.setCapacity(room.getCapacity());
        dto.setType(room.getType());
        dto.setPrice(room.getPrice());
        dto.setAvailable(room.getAvailable());

        if (room.getHotel() != null) {
            dto.setHotelId(room.getHotel().getId());
            dto.setHotelName(room.getHotel().getName());
        }

        if (room.getAmenities() != null && !room.getAmenities().isEmpty()) {
            dto.setAmenityIds(room.getAmenities().stream()
                    .map(Amenity::getId)
                    .collect(Collectors.toList()));
        }

        return dto;
    }

    public static Room toEntity(RoomRequestDTO dto) {
        if (dto == null) {
            return null;
        }

        Room room = new Room();
        room.setRoomNumber(dto.getRoomNumber());
        room.setFloor(dto.getFloor());
        room.setCapacity(dto.getCapacity());
        room.setType(dto.getType());
        room.setPrice(dto.getPrice());
        room.setAvailable(dto.getAvailable() != null ? dto.getAvailable() : true);

        return room;
    }
}