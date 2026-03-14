package com.example.hotelbooking.repository;

import com.example.hotelbooking.entity.Hotel;
import org.springframework.data.jpa.repository.EntityGraph;
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

    @EntityGraph(attributePaths = {"rooms", "amenities"})
    @Query("SELECT h FROM Hotel h")
    List<Hotel> findAllWithDetails();

    @Override
    @EntityGraph(attributePaths = {"rooms", "amenities"})
    Optional<Hotel> findById(Long id);
}