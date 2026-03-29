package com.example.hotelbooking.service;

import com.example.hotelbooking.cache.HotelCache;
import com.example.hotelbooking.cache.HotelSearchKey;
import com.example.hotelbooking.dto.HotelResponseDTO;
import com.example.hotelbooking.entity.Hotel;
import com.example.hotelbooking.mapper.HotelMapper;
import com.example.hotelbooking.repository.HotelRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class HotelCachingService {

    private final HotelRepository hotelRepository;
    private final HotelMapper hotelMapper;
    private final HotelCache hotelCache;
    private HotelCachingService self;

    @PostConstruct
    public void init() {
        this.self = this;
    }

    private Pageable createPageable(int page, int size) {
        return PageRequest.of(page, size, Sort.by("name").ascending());
    }

    @Transactional(readOnly = true)
    public Page<HotelResponseDTO> findHotelsByRoomTypeAndPrice(
            String roomType, Double minPrice, int page, int size) {

        Pageable pageable = createPageable(page, size);
        Page<Hotel> hotelPage = hotelRepository.findHotelsByRoomTypeAndPrice(roomType, minPrice, pageable);

        return hotelPage.map(hotelMapper::toResponseDTO);
    }

    public Page<HotelResponseDTO> findHotelsByRoomTypeAndPriceCached(
            String roomType, Double minPrice, int page, int size) {

        HotelSearchKey key = new HotelSearchKey(roomType, minPrice, null, page, size, "name");

        Page<HotelResponseDTO> cached = hotelCache.get(key);
        if (cached != null) {
            log.info("Кэш HIT: roomType={}, minPrice={}, page={}, size={}", roomType, minPrice, page, size);
            return cached;
        }

        log.info("Кэш MISS: выполняем запрос к БД для roomType={}, minPrice={}, page={}, size={}",
                roomType, minPrice, page, size);

        Page<HotelResponseDTO> result = self.findHotelsByRoomTypeAndPrice(roomType, minPrice, page, size);

        hotelCache.put(key, result);
        log.info("Результат сохранён в кэш");

        return result;
    }

    @Transactional(readOnly = true)
    public Page<HotelResponseDTO> findHotelsByRoomTypeAndPriceNative(
            String roomType, Double minPrice, int page, int size) {

        HotelSearchKey key = new HotelSearchKey(roomType, minPrice, null, page, size, "name-native");
        Page<HotelResponseDTO> cached = hotelCache.get(key);
        if (cached != null) {
            log.info("Кэш HIT (Native): roomType={}, minPrice={}, page={}, size={}", roomType, minPrice, page, size);
            return cached;
        }

        log.info("Кэш MISS (Native): выполняем запрос к БД для roomType={}, minPrice={}", roomType, minPrice);
        Pageable pageable = createPageable(page, size);

        Page<Long> idsPage = hotelRepository.findHotelIdsByRoomTypeAndPriceNative(roomType, minPrice, pageable);

        Page<Hotel> hotelPage;
        if (idsPage.isEmpty()) {
            hotelPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
        } else {
            List<Hotel> unsortedHotels = hotelRepository.findAllWithDetailsByIds(idsPage.getContent());

            java.util.Map<Long, Hotel> hotelMap = unsortedHotels.stream()
                    .collect(java.util.stream.Collectors.toMap(Hotel::getId, h -> h));

            List<Hotel> sortedHotels = idsPage.getContent().stream()
                    .map(hotelMap::get)
                    .toList();

            hotelPage = new PageImpl<>(sortedHotels, pageable, idsPage.getTotalElements());
        }

        Page<HotelResponseDTO> result = hotelPage.map(hotelMapper::toResponseDTO);

        hotelCache.put(key, result);
        log.info("Результат (Native) сохранён в кэш");

        return result;
    }

    public void invalidateByHotelId(Long hotelId) {
        log.info("Инвалидация кэша для отеля с ID: {}", hotelId);
        hotelCache.clearByHotelId(hotelId);
    }

    public void invalidateAll() {
        log.info("Полная очистка кэша");
        hotelCache.clearAll();
    }
}