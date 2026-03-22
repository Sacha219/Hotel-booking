package com.example.hotelbooking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomResponseDTO {
    private Long id;
    private String number;
    private Integer floor;
    private Integer capacity;
    private String type;
    private Double price;
    private Boolean available;
    private Long hotelId;
    private String hotelName;
    private List<Long> amenityIds;
}