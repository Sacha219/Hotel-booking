package com.example.hotelbooking.cache;

import com.example.hotelbooking.dto.HotelResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class HotelCache {

    private final Map<HotelSearchKey, Page<HotelResponseDTO>> cache = new ConcurrentHashMap<>();

    public Page<HotelResponseDTO> get(HotelSearchKey key) {
        return cache.get(key);
    }

    public void put(HotelSearchKey key, Page<HotelResponseDTO> value) {
        cache.put(key, value);
    }

    public void clearByHotelId(Long hotelId) {
        cache.keySet().removeIf(key -> key.getHotelId() != null && key.getHotelId().equals(hotelId));
    }

    public void clearAll() {
        cache.clear();
    }
}