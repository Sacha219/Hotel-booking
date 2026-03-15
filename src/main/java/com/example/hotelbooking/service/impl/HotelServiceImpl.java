package com.example.hotelbooking.service.impl;

import com.example.hotelbooking.dto.HotelResponseDTO;
import com.example.hotelbooking.entity.Hotel;
import com.example.hotelbooking.mapper.HotelMapper;
import com.example.hotelbooking.repository.HotelRepository;
import com.example.hotelbooking.service.HotelService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HotelServiceImpl implements HotelService {

    private final HotelRepository hotelRepository;
    private final HotelMapper hotelMapper;

    @Override
    @Transactional(readOnly = true)
    public HotelResponseDTO getHotelById(Long id) {
        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Отель с идентификатором " + id + " не найден"));
        return hotelMapper.toResponseDTO(hotel);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HotelResponseDTO> getAllHotels() {
        return hotelRepository.findAll().stream()
                .map(hotelMapper::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<HotelResponseDTO> getHotelsByCity(String city) {
        return hotelRepository.findByCityIgnoreCase(city).stream()
                .map(hotelMapper::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<HotelResponseDTO> getHotelsByStars(Integer stars) {
        return hotelRepository.findByStars(stars).stream()
                .map(hotelMapper::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<HotelResponseDTO> getHotelsByCityAndStars(String city, Integer stars) {
        return hotelRepository.findByCityIgnoreCaseAndStars(city, stars).stream()
                .map(hotelMapper::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<HotelResponseDTO> getAllHotelsPlain() {
        return hotelRepository.findAllPlain().stream()
                .map(hotelMapper::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<HotelResponseDTO> getAllHotelsWithDetails() {
        return hotelRepository.findAllWithDetails().stream()
                .map(hotelMapper::toResponseDTO)
                .toList();
    }
}