package com.example.hotelbooking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HotelResponseDTO {
    private Long id;
    private String name;
    private String address;
    private String city;
    private Integer stars;
    private String description;
    private Boolean available;
    private List<Long> roomIds;
    private List<Long> amenityIds;
}