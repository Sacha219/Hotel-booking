package com.example.hotelbooking.repository;

import com.example.hotelbooking.entity.Booking;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByGuestId(Long guestId);

    List<Booking> findByRoomId(Long roomId);

    List<Booking> findByStatusIgnoreCase(String status);

    @Query("SELECT b FROM Booking b WHERE b.checkInDate <= ?2 AND b.checkOutDate >= ?1")
    List<Booking> findOverlappingBookings(LocalDate checkIn, LocalDate checkOut);

    @Override
    @EntityGraph(attributePaths = {"guest", "room"})
    List<Booking> findAll();
}