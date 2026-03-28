package com.example.hotelbooking.cache;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@EqualsAndHashCode
public class HotelSearchKey {
    private final String roomType;
    private final Double minPrice;
    private final Long hotelId;
    private final int page;
    private final int size;
    private final String sortField;
}