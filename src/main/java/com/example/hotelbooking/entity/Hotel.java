package com.example.hotelbooking.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Hotel {

    private Long id;
    private String name;
    private String address;
    private String city;
    private String country;
    private Integer stars;
    private String description;
    private Double pricePerNight;
    private Boolean available;
}