package com.example.hotelbooking.service;

import com.example.hotelbooking.dto.RoomRequestDTO;
import com.example.hotelbooking.dto.RoomResponseDTO;
import com.example.hotelbooking.entity.Hotel;
import com.example.hotelbooking.entity.Room;
import com.example.hotelbooking.mapper.RoomMapper;
import com.example.hotelbooking.repository.HotelRepository;
import com.example.hotelbooking.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;
    private final HotelRepository hotelRepository;

    @Transactional(readOnly = true)
    public List<RoomResponseDTO> findAll() {
        return roomRepository.findAll().stream()
                .map(RoomMapper::toResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public RoomResponseDTO findById(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException(" Room not found with id: " + id));
        return RoomMapper.toResponseDTO(room);
    }

    @Transactional(readOnly = true)
    public List<RoomResponseDTO> findByHotelId(Long hotelId) {
        return roomRepository.findByHotelId(hotelId).stream()
                .map(RoomMapper::toResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RoomResponseDTO> findAvailableRooms(Long hotelId, LocalDate checkIn, LocalDate checkOut) {
        return roomRepository.findAvailableRooms(hotelId, checkIn, checkOut).stream()
                .map(RoomMapper::toResponseDTO)
                .toList();
    }

    @Transactional
    public RoomResponseDTO create(RoomRequestDTO dto) {
        Hotel hotel = hotelRepository.findById(dto.getHotelId())
                .orElseThrow(() -> new NoSuchElementException("Hotel not found with id:  " + dto.getHotelId()));

        Room room = RoomMapper.toEntity(dto);
        room.setHotel(hotel);

        Room savedRoom = roomRepository.save(room);
        return RoomMapper.toResponseDTO(savedRoom);
    }

    @Transactional
    public RoomResponseDTO update(Long id, RoomRequestDTO dto) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Room not found with id:" + id));

        room.setNumber(dto.getNumber());
        room.setFloor(dto.getFloor());
        room.setCapacity(dto.getCapacity());
        room.setType(dto.getType());
        room.setPrice(dto.getPrice());
        room.setImageUrl(dto.getImageUrl());
        room.setAvailable(dto.getAvailable() != null ? dto.getAvailable() : room.getAvailable());

        Room updatedRoom = roomRepository.save(room);
        return RoomMapper.toResponseDTO(updatedRoom);
    }

    @Transactional
    public void delete(Long id) {
        if (!roomRepository.existsById(id)) {
            throw new NoSuchElementException("Room not found  with id: " + id);
        }
        roomRepository.deleteById(id);
    }
}
