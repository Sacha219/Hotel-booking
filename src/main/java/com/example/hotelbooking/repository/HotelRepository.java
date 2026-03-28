package com.example.hotelbooking.repository;

import com.example.hotelbooking.entity.Hotel;
import jakarta.annotation.Nonnull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    @Query("SELECT DISTINCT h FROM Hotel h " +
            "JOIN h.rooms r " +
            "WHERE r.type = :roomType AND r.price >= :minPrice")
    Page<Hotel> findHotelsByRoomTypeAndPrice(@Param("roomType") String roomType,
                                             @Param("minPrice") Double minPrice,
                                             Pageable pageable);

    @Query(value = "SELECT DISTINCT h.* FROM hotels h " +
            "INNER JOIN rooms r ON h.id = r.hotel_id " +
            "WHERE r.type = :roomType AND r.price >= :minPrice",
            countQuery = "SELECT COUNT(DISTINCT h.id) FROM hotels h " +
                    "INNER JOIN rooms r ON h.id = r.hotel_id " +
                    "WHERE r.type = :roomType AND r.price >= :minPrice",
            nativeQuery = true)
    Page<Hotel> findHotelsByRoomTypeAndPriceNative(@Param("roomType") String roomType,
                                                   @Param("minPrice") Double minPrice,
                                                   Pageable pageable);
}