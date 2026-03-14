package com.example.hotelbooking.mapper;

import com.example.hotelbooking.dto.AmenityDTO;
import com.example.hotelbooking.entity.Amenity;

public class AmenityMapper {

    private AmenityMapper() {

    }

    public static AmenityDTO toDTO(Amenity amenity) {
        if (amenity == null) {
            return null;
        }

        AmenityDTO dto = new AmenityDTO();
        dto.setId(amenity.getId());
        dto.setName(amenity.getName());
        dto.setDescription(amenity.getDescription());
        dto.setIcon(amenity.getIcon());

        return dto;
    }

    public static Amenity toEntity(AmenityDTO dto) {
        if (dto == null) {
            return null;
        }

        Amenity amenity = new Amenity();
        amenity.setId(dto.getId());
        amenity.setName(dto.getName());
        amenity.setDescription(dto.getDescription());
        amenity.setIcon(dto.getIcon());

        return amenity;
    }
}