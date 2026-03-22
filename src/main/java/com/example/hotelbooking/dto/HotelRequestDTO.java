package com.example.hotelbooking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HotelRequestDTO {
    private String name;
    private String address;
    private String city;
    private Integer stars;
    private String description;
    private Boolean available;
}