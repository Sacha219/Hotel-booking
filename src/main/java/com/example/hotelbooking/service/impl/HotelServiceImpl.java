package com.example.hotelbooking.service.impl;

import com.example.hotelbooking.dto.HotelRequestDTO;
import com.example.hotelbooking.dto.HotelResponseDTO;
import com.example.hotelbooking.entity.Hotel;
import com.example.hotelbooking.mapper.HotelMapper;
import com.example.hotelbooking.repository.HotelRepository;
import com.example.hotelbooking.service.HotelService;
import com.example.hotelbooking.service.HotelCachingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Slf4j
@Service
@RequiredArgsConstructor
public class HotelServiceImpl implements HotelService {

    private final HotelRepository hotelRepository;
    private final HotelMapper hotelMapper;
    private final HotelCachingService hotelCachingService;

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

    @Override
    @Transactional(readOnly = true)
    public Page<HotelResponseDTO> findHotelsByRoomTypeAndPrice(String roomType, Double minPrice, int page, int size) {
        return hotelCachingService.findHotelsByRoomTypeAndPriceCached(roomType, minPrice, page, size);
    }

    @Override
    @Transactional
    public HotelResponseDTO createHotel(HotelRequestDTO hotelRequestDTO) {
        Hotel hotel = hotelMapper.toEntity(hotelRequestDTO);
        hotel.setAvailable(true);
        Hotel savedHotel = hotelRepository.save(hotel);

        hotelCachingService.invalidateAll();
        log.info("Кэш инвалидирован после создания отеля");

        return hotelMapper.toResponseDTO(savedHotel);
    }

    @Override
    @Transactional
    public HotelResponseDTO updateHotel(Long id, HotelRequestDTO hotelRequestDTO) {
        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Отель с идентификатором " + id + " не найден"));

        hotel.setName(hotelRequestDTO.getName());
        hotel.setAddress(hotelRequestDTO.getAddress());
        hotel.setCity(hotelRequestDTO.getCity());
        hotel.setStars(hotelRequestDTO.getStars());
        hotel.setDescription(hotelRequestDTO.getDescription());
        hotel.setAvailable(hotelRequestDTO.getAvailable() != null ? hotelRequestDTO.getAvailable() : hotel.getAvailable());

        Hotel updatedHotel = hotelRepository.save(hotel);

        hotelCachingService.invalidateByHotelId(id);
        log.info("Кэш инвалидирован после обновления отеля ID: {}", id);

        return hotelMapper.toResponseDTO(updatedHotel);
    }

    @Override
    @Transactional
    public void deleteHotel(Long id) {
        if (!hotelRepository.existsById(id)) {
            throw new NoSuchElementException("Отель с идентификатором " + id + " не найден");
        }
        hotelRepository.deleteById(id);

        hotelCachingService.invalidateByHotelId(id);
        log.info("Кэш инвалидирован после удаления отеля ID: {}", id);
    }
}