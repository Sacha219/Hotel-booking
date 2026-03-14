package com.example.hotelbooking.service;

import com.example.hotelbooking.dto.GuestRequestDTO;
import com.example.hotelbooking.dto.GuestResponseDTO;
import com.example.hotelbooking.entity.Guest;
import com.example.hotelbooking.exception.TransactionDemoException;
import com.example.hotelbooking.mapper.GuestMapper;
import com.example.hotelbooking.repository.GuestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GuestService {

    private final GuestRepository guestRepository;

    @Transactional(readOnly = true)
    public List<GuestResponseDTO> findAll() {
        return guestRepository.findAll().stream()
                .map(GuestMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public GuestResponseDTO findById(Long id) {
        Guest guest = guestRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Guest not found with id: " + id));
        return GuestMapper.toResponseDTO(guest);
    }

    @Transactional(readOnly = true)
    public GuestResponseDTO findByEmail(String email) {
        Guest guest = guestRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new NoSuchElementException("Guest not found with email: " + email));
        return GuestMapper.toResponseDTO(guest);
    }

    @Transactional
    public GuestResponseDTO create(GuestRequestDTO dto) {
        if (guestRepository.existsByEmailIgnoreCase(dto.getEmail())) {
            throw new IllegalArgumentException("Guest with email " + dto.getEmail() + " already exists");
        }

        Guest guest = GuestMapper.toEntity(dto);
        Guest savedGuest = guestRepository.save(guest);
        return GuestMapper.toResponseDTO(savedGuest);
    }

    public GuestResponseDTO createWithoutTransaction(GuestRequestDTO dto) {
        Guest guest = GuestMapper.toEntity(dto);
        Guest savedGuest = guestRepository.save(guest);

        throw new TransactionDemoException("Ошибка после сохранения гостя (БЕЗ @Transactional)");
    }

    @Transactional
    public GuestResponseDTO createWithTransaction(GuestRequestDTO dto) {
        Guest guest = GuestMapper.toEntity(dto);
        Guest savedGuest = guestRepository.save(guest);

        throw new TransactionDemoException("Ошибка после сохранения гостя (С @Transactional)");
    }

    @Transactional
    public GuestResponseDTO update(Long id, GuestRequestDTO dto) {
        Guest guest = guestRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Guest not found with id: " + id));

        guest.setFirstName(dto.getFirstName());
        guest.setLastName(dto.getLastName());
        guest.setPhone(dto.getPhone());

        if (!guest.getEmail().equalsIgnoreCase(dto.getEmail())) {
            if (guestRepository.existsByEmailIgnoreCase(dto.getEmail())) {
                throw new IllegalArgumentException("Guest with email " + dto.getEmail() + " already exists");
            }
            guest.setEmail(dto.getEmail());
        }

        Guest updatedGuest = guestRepository.save(guest);
        return GuestMapper.toResponseDTO(updatedGuest);
    }

    @Transactional
    public void delete(Long id) {
        if (!guestRepository.existsById(id)) {
            throw new NoSuchElementException("Guest not found with id: " + id);
        }
        guestRepository.deleteById(id);
    }
}