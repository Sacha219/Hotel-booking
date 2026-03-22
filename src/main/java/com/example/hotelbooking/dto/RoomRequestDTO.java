package com.example.hotelbooking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomRequestDTO {
    private String number;
    private Integer floor;
    private Integer capacity;
    private String type;
    private Double price;
    private Boolean available;
    private Long hotelId;
}