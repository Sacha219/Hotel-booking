package com.example.hotelbooking.config;

import com.example.hotelbooking.entity.Amenity;
import com.example.hotelbooking.entity.Booking;
import com.example.hotelbooking.entity.Guest;
import com.example.hotelbooking.entity.Hotel;
import com.example.hotelbooking.entity.Room;
import com.example.hotelbooking.repository.AmenityRepository;
import com.example.hotelbooking.repository.BookingRepository;
import com.example.hotelbooking.repository.GuestRepository;
import com.example.hotelbooking.repository.HotelRepository;
import com.example.hotelbooking.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepository;
    private final AmenityRepository amenityRepository;
    private final GuestRepository guestRepository;
    private final BookingRepository bookingRepository;

    @Override
    public void run(String... args) {

        if (hotelRepository.count() == 0) {
            createAmenities();
            createHotels();
            createRooms();
            createGuests();
            createBookings();
        }
    }

    private void createAmenities() {
        Amenity wifi = new Amenity();
        wifi.setName("WiFi");
        wifi.setDescription("Бесплатный высокоскоростной интернет");
        wifi.setIcon("wifi");
        amenityRepository.save(wifi);

        Amenity parking = new Amenity();
        parking.setName("Parking");
        parking.setDescription("Бесплатная парковка");
        parking.setIcon("parking");
        amenityRepository.save(parking);

        Amenity pool = new Amenity();
        pool.setName("Pool");
        pool.setDescription("Открытый бассейн");
        pool.setIcon("pool");
        amenityRepository.save(pool);

        Amenity gym = new Amenity();
        gym.setName("Gym");
        gym.setDescription("Тренажерный зал");
        gym.setIcon("gym");
        amenityRepository.save(gym);

        Amenity breakfast = new Amenity();
        breakfast.setName("Breakfast");
        breakfast.setDescription("Завтрак включён");
        breakfast.setIcon("breakfast");
        amenityRepository.save(breakfast);
    }

    private void createHotels() {
        Hotel hotel1 = new Hotel();
        hotel1.setName("Grand Hotel Moscow");
        hotel1.setAddress("ул. Тверская, 10");
        hotel1.setCity("Москва");
        hotel1.setCountry("Россия");
        hotel1.setStars(5);
        hotel1.setDescription("Роскошный отель в центре Москвы");
        hotel1.setPricePerNight(15000.0);
        hotel1.setAvailable(true);
        hotelRepository.save(hotel1);

        Hotel hotel2 = new Hotel();
        hotel2.setName("Бизнес Отель");
        hotel2.setAddress("ул. Новый Арбат, 15");
        hotel2.setCity("Москва");
        hotel2.setCountry("Россия");
        hotel2.setStars(4);
        hotel2.setDescription("Удобный отель для деловых поездок");
        hotel2.setPricePerNight(8000.0);
        hotel2.setAvailable(true);
        hotelRepository.save(hotel2);

        Hotel hotel3 = new Hotel();
        hotel3.setName("Питер Отель");
        hotel3.setAddress("Невский проспект, 20");
        hotel3.setCity("Санкт-Петербург");
        hotel3.setCountry("Россия");
        hotel3.setStars(4);
        hotel3.setDescription("Отель в культурной столице");
        hotel3.setPricePerNight(9000.0);
        hotel3.setAvailable(true);
        hotelRepository.save(hotel3);
    }

    private void createRooms() {
        List<Amenity> amenities = amenityRepository.findAll();
        List<Hotel> hotels = hotelRepository.findAll();

        for (Hotel hotel : hotels) {
            for (int i = 1; i <= 5; i++) {
                Room room = new Room();
                room.setRoomNumber(String.valueOf(100 + i));
                room.setFloor(1);
                room.setCapacity(2);
                room.setType(i % 2 == 0 ? "STANDARD" : "DELUXE");
                room.setPrice(hotel.getPricePerNight() * (0.8 + i * 0.1));
                room.setAvailable(true);
                room.setHotel(hotel);

                if (i % 2 == 0) {
                    room.setAmenities((Set<Amenity>) amenities.subList(0, 2));
                } else {
                    room.setAmenities((Set<Amenity>) amenities.subList(2, 4));
                }

                roomRepository.save(room);
            }
        }
    }

    private void createGuests() {
        Guest guest1 = new Guest();
        guest1.setFirstName("Иван");
        guest1.setLastName("Петров");
        guest1.setEmail("ivan@email.com");
        guest1.setPhone("+7-999-123-45-67");
        guest1.setRegistrationDate(LocalDate.now().minusMonths(1));
        guestRepository.save(guest1);

        Guest guest2 = new Guest();
        guest2.setFirstName("Мария");
        guest2.setLastName("Иванова");
        guest2.setEmail("maria@email.com");
        guest2.setPhone("+7-999-765-43-21");
        guest2.setRegistrationDate(LocalDate.now().minusWeeks(2));
        guestRepository.save(guest2);

        Guest guest3 = new Guest();
        guest3.setFirstName("Алексей");
        guest3.setLastName("Сидоров");
        guest3.setEmail("alex@email.com");
        guest3.setPhone("+7-999-555-55-55");
        guest3.setRegistrationDate(LocalDate.now().minusDays(5));
        guestRepository.save(guest3);
    }

    private void createBookings() {
        List<Guest> guests = guestRepository.findAll();
        List<Room> rooms = roomRepository.findAll();

        Booking booking1 = new Booking();
        booking1.setCheckInDate(LocalDate.now().plusDays(10));
        booking1.setCheckOutDate(LocalDate.now().plusDays(15));
        booking1.setStatus("CONFIRMED");
        booking1.setRoom(rooms.get(0));
        booking1.setGuest(guests.get(0));
        booking1.setTotalPrice(rooms.get(0).getPrice() * 5);
        bookingRepository.save(booking1);

        Booking booking2 = new Booking();
        booking2.setCheckInDate(LocalDate.now().plusDays(20));
        booking2.setCheckOutDate(LocalDate.now().plusDays(25));
        booking2.setStatus("CONFIRMED");
        booking2.setRoom(rooms.get(1));
        booking2.setGuest(guests.get(1));
        booking2.setTotalPrice(rooms.get(1).getPrice() * 5);
        bookingRepository.save(booking2);

        Booking booking3 = new Booking();
        booking3.setCheckInDate(LocalDate.now().plusDays(5));
        booking3.setCheckOutDate(LocalDate.now().plusDays(7));
        booking3.setStatus("PENDING");
        booking3.setRoom(rooms.get(2));
        booking3.setGuest(guests.get(2));
        booking3.setTotalPrice(rooms.get(2).getPrice() * 2);
        bookingRepository.save(booking3);
    }
}