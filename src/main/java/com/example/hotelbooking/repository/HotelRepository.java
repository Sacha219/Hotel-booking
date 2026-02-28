package com.example.hotelbooking.repository;

import com.example.hotelbooking.entity.Hotel;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class HotelRepository {

    private final Map<Long, Hotel> hotels = new ConcurrentHashMap<>();

    private final AtomicLong idGenerator = new AtomicLong(1);

    public Hotel save(Hotel hotel) {
        if (hotel.getId() == null) {
            hotel.setId(idGenerator.getAndIncrement());
        }
        hotels.put(hotel.getId(), hotel);
        return hotel;
    }

    public Optional<Hotel> findById(Long id) {
        return Optional.ofNullable(hotels.get(id));
    }

    public List<Hotel> findAll() {
        return new ArrayList<>(hotels.values());
    }

    public List<Hotel> findByCity(String city) {
        List<Hotel> result = new ArrayList<>();
        for (Hotel hotel : hotels.values()) {
            if (hotel.getCity().equalsIgnoreCase(city)) {
                result.add(hotel);
            }
        }
        return result;
    }

    public List<Hotel> findByStars(Integer stars) {
        List<Hotel> result = new ArrayList<>();
        for (Hotel hotel : hotels.values()) {
            if (hotel.getStars().equals(stars)) {
                result.add(hotel);
            }
        }
        return result;
    }

    public List<Hotel> findByCityAndStars(String city, Integer stars) {
        List<Hotel> result = new ArrayList<>();
        for (Hotel hotel : hotels.values()) {
            if (hotel.getCity().equalsIgnoreCase(city) &&
                    hotel.getStars().equals(stars)) {
                result.add(hotel);
            }
        }
        return result;
    }

    public void deleteById(Long id) {
        hotels.remove(id);
    }

    public boolean existsById(Long id) {
        return hotels.containsKey(id);
    }
}