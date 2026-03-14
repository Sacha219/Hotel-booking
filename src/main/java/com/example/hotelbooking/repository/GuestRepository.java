package com.example.hotelbooking.repository;

import com.example.hotelbooking.entity.Guest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GuestRepository extends JpaRepository<Guest, Long> {
    Optional<Guest> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);
}