package com.example.hotelbooking.repository;

import com.example.hotelbooking.entity.Hotel;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class HotelRepository {

    private final Map<Long, Hotel> hotels = new ConcurrentHashMap<>();

    public HotelRepository() {

        Hotel hotel1 = new Hotel();
        hotel1.setId(1L);
        hotel1.setName("Grand Hotel Moscow");
        hotel1.setAddress("ул. Тверская, 10");
        hotel1.setCity("Москва");
        hotel1.setCountry("Россия");
        hotel1.setStars(5);
        hotel1.setDescription("Роскошный отель в центре Москвы");
        hotel1.setPricePerNight(15000.0);
        hotel1.setAvailable(true);
        hotels.put(1L, hotel1);

        Hotel hotel2 = new Hotel();
        hotel2.setId(2L);
        hotel2.setName("Бизнес Отель");
        hotel2.setAddress("ул. Новый Арбат, 15");
        hotel2.setCity("Москва");
        hotel2.setCountry("Россия");
        hotel2.setStars(4);
        hotel2.setDescription("Удобный отель для деловых поездок");
        hotel2.setPricePerNight(8000.0);
        hotel2.setAvailable(true);
        hotels.put(2L, hotel2);

        Hotel hotel3 = new Hotel();
        hotel3.setId(3L);
        hotel3.setName("Отель на Ленинском");
        hotel3.setAddress("Ленинский проспект, 45");
        hotel3.setCity("Москва");
        hotel3.setCountry("Россия");
        hotel3.setStars(3);
        hotel3.setDescription("Бюджетный вариант рядом с метро");
        hotel3.setPricePerNight(3500.0);
        hotel3.setAvailable(true);
        hotels.put(3L, hotel3);
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
}