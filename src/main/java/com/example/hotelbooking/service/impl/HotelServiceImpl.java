package com.example.hotelbooking.service.impl;

import com.example.hotelbooking.dto.HotelRequestDTO;
import com.example.hotelbooking.dto.HotelResponseDTO;
import com.example.hotelbooking.entity.Hotel;
import com.example.hotelbooking.mapper.HotelMapper;
import com.example.hotelbooking.repository.HotelRepository;
import com.example.hotelbooking.service.HotelService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class HotelServiceImpl implements HotelService {

    private final HotelRepository hotelRepository;
    private final HotelMapper hotelMapper;

    @Override
    public HotelResponseDTO createHotel(HotelRequestDTO hotelRequestDTO) {
        Hotel hotel = hotelMapper.toEntity(hotelRequestDTO);
        hotel.setAvailable(true);
        Hotel savedHotel = hotelRepository.save(hotel);
        return hotelMapper.toResponseDTO(savedHotel);
    }

    @Override
    public HotelResponseDTO getHotelById(Long id) {
        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Отель с идентификатором " + id + " не найден"));
        return hotelMapper.toResponseDTO(hotel);
    }

    @Override
    public List<HotelResponseDTO> getAllHotels() {
        return hotelRepository.findAll().stream()
                .map(hotelMapper::toResponseDTO)
                .toList();
    }

    @Override
    public List<HotelResponseDTO> getHotelsByCity(String city) {
        return hotelRepository.findByCity(city).stream()
                .map(hotelMapper::toResponseDTO)
                .toList();
    }

    @Override
    public List<HotelResponseDTO> getHotelsByStars(Integer stars) {
        return hotelRepository.findByStars(stars).stream()
                .map(hotelMapper::toResponseDTO)
                .toList();
    }

    @Override
    public List<HotelResponseDTO> getHotelsByCityAndStars(String city, Integer stars) {
        return hotelRepository.findByCityAndStars(city, stars).stream()
                .map(hotelMapper::toResponseDTO)
                .toList();
    }

    @Override
    public HotelResponseDTO updateHotel(Long id, HotelRequestDTO hotelRequestDTO) {
        Hotel existingHotel = hotelRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Отель с идентификатором " + id + " не найден"));

        existingHotel.setName(hotelRequestDTO.getName());
        existingHotel.setAddress(hotelRequestDTO.getAddress());
        existingHotel.setCity(hotelRequestDTO.getCity());
        existingHotel.setCountry(hotelRequestDTO.getCountry());
        existingHotel.setStars(hotelRequestDTO.getStars());
        existingHotel.setDescription(hotelRequestDTO.getDescription());
        existingHotel.setPricePerNight(hotelRequestDTO.getPricePerNight());

        if (hotelRequestDTO.getAvailable() != null) {
            existingHotel.setAvailable(hotelRequestDTO.getAvailable());
        }

        Hotel updatedHotel = hotelRepository.save(existingHotel);
        return hotelMapper.toResponseDTO(updatedHotel);
    }

    @Override
    public void deleteHotel(Long id) {
        if (!hotelRepository.existsById(id)) {
            throw new NoSuchElementException("Отель с идентификатором " + id + " не найден");
        }
        hotelRepository.deleteById(id);
    }
}