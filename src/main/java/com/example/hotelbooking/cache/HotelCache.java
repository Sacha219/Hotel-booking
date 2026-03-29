package com.example.hotelbooking.cache;

import com.example.hotelbooking.dto.HotelResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class HotelCache {

    private final Map<HotelSearchKey, Page<HotelResponseDTO>> cache = new HashMap<>();

    public synchronized Page<HotelResponseDTO> get(HotelSearchKey key) {
        return cache.get(key);
    }

    public synchronized void put(HotelSearchKey key, Page<HotelResponseDTO> value) {
        cache.put(key, value);
    }

    public synchronized void clearByHotelId(Long hotelId) {
        cache.keySet().removeIf(key -> key.getHotelId() != null && key.getHotelId().equals(hotelId));
    }

    public synchronized void clearAll() {
        cache.clear();
    }
}