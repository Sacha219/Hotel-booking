package com.example.hotelbooking.repository;

import com.example.hotelbooking.entity.Hotel;
import jakarta.annotation.Nonnull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HotelRepository extends JpaRepository<Hotel, Long> {

    List<Hotel> findByCityIgnoreCase(String city);

    List<Hotel> findByStars(Integer stars);

    List<Hotel> findByCityIgnoreCaseAndStars(String city, Integer stars);

    @Query("SELECT h FROM Hotel h")
    List<Hotel> findAllPlain();

    @Query("SELECT DISTINCT h FROM Hotel h LEFT JOIN FETCH h.rooms LEFT JOIN FETCH h.amenities")
    List<Hotel> findAllWithDetails();

    @Override
    @Nonnull
    @Query("SELECT DISTINCT h FROM Hotel h LEFT JOIN FETCH h.rooms LEFT JOIN FETCH h.amenities WHERE h.id = :id")
    Optional<Hotel> findById(@Nonnull Long id);
}