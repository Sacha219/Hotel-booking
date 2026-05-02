package com.example.hotelbooking.service;

import com.example.hotelbooking.dto.AmenityDTO;
import com.example.hotelbooking.entity.Amenity;
import com.example.hotelbooking.entity.Hotel;
import com.example.hotelbooking.entity.Room;
import com.example.hotelbooking.mapper.AmenityMapper;
import com.example.hotelbooking.repository.AmenityRepository;
import com.example.hotelbooking.repository.HotelRepository;
import com.example.hotelbooking.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class AmenityService {

    private final AmenityRepository amenityRepository;
    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepository;

    @Transactional(readOnly = true)
    public List<AmenityDTO> findAll() {
        return amenityRepository.findAll().stream()
                .map(AmenityMapper::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public AmenityDTO findById(Long id) {
        Amenity amenity = amenityRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Amenity not found with id: " + id));
        return AmenityMapper.toDTO(amenity);
    }

    @Transactional(readOnly = true)
    public AmenityDTO findByName(String name) {
        Amenity amenity = amenityRepository.findByNameIgnoreCase(name)
                .orElseThrow(() -> new NoSuchElementException("Amenity not found with name: " + name));
        return AmenityMapper.toDTO(amenity);
    }

    @Transactional
    public AmenityDTO create(AmenityDTO dto) {
        if (amenityRepository.existsByNameIgnoreCase(dto.getName())) {
            throw new IllegalArgumentException("Amenity with name " + dto.getName() + " already exists");
        }

        Amenity amenity = AmenityMapper.toEntity(dto);
        Amenity savedAmenity = amenityRepository.save(amenity);
        return AmenityMapper.toDTO(savedAmenity);
    }

    @Transactional
    public AmenityDTO update(Long id, AmenityDTO dto) {
        Amenity amenity = amenityRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Amenity not  found with id:" + id));

        amenity.setName(dto.getName());
        amenity.setDescription(dto.getDescription());
        amenity.setIcon(dto.getIcon());

        Amenity updatedAmenity = amenityRepository.save(amenity);
        return AmenityMapper.toDTO(updatedAmenity);
    }

    @Transactional
    public void delete(Long id) {
        Amenity amenity = amenityRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException(" Amenity not found"));

        for (Hotel hotel : hotelRepository.findAll()) {
            hotel.getAmenities().remove(amenity);
        }

        for (Room room : amenity.getRooms()) {
            room.getAmenities().remove(amenity);
        }
        amenity.getRooms().clear();

        amenityRepository.delete(amenity);
    }

    @Transactional
    public void addAmenityToHotel(Long amenityId, Long hotelId) {
        Amenity amenity = amenityRepository.findById(amenityId)
                .orElseThrow(() -> new NoSuchElementException("  Amenity not found"));
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new NoSuchElementException("Hotel not found"));
        hotel.getAmenities().add(amenity);
        hotelRepository.save(hotel);
    }

    @Transactional
    public void addAmenityToRoom(Long amenityId, Long roomId) {
        Amenity amenity = amenityRepository.findById(amenityId)
                .orElseThrow(() -> new NoSuchElementException("Amenity  not found"));
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new NoSuchElementException("Room not found"));
        room.getAmenities().add(amenity);
        roomRepository.save(room);
    }

    @Transactional
    public void removeAmenityFromHotel(Long amenityId, Long hotelId) {
        Amenity amenity = amenityRepository.findById(amenityId)
                .orElseThrow(() -> new NoSuchElementException("Amenity not found  "));
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new NoSuchElementException("Hotel not found"));
        hotel.getAmenities().remove(amenity);
        hotelRepository.save(hotel);
    }

    @Transactional
    public void removeAmenityFromRoom(Long amenityId, Long roomId) {
        Amenity amenity = amenityRepository.findById(amenityId)
                .orElseThrow(() -> new NoSuchElementException("Amenity not  found"));
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new NoSuchElementException("Room not found"));
        room.getAmenities().remove(amenity);
        roomRepository.save(room);
    }
}
