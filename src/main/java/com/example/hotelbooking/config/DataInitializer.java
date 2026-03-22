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
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
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
    public void run(@Nonnull String... args) {

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
        parking.setName("Парковка");
        parking.setDescription("Бесплатная охраняемая парковка");
        parking.setIcon("parking");
        amenityRepository.save(parking);

        Amenity pool = new Amenity();
        pool.setName("Бассейн");
        pool.setDescription("Крытый подогреваемый бассейн");
        pool.setIcon("pool");
        amenityRepository.save(pool);

        Amenity gym = new Amenity();
        gym.setName("Фитнес-центр");
        gym.setDescription("Современный тренажерный зал");
        gym.setIcon("gym");
        amenityRepository.save(gym);

        Amenity breakfast = new Amenity();
        breakfast.setName("Завтрак");
        breakfast.setDescription("Завтрак включён в стоимость");
        breakfast.setIcon("breakfast");
        amenityRepository.save(breakfast);
    }

    private void createHotels() {
        Hotel hotel1 = new Hotel();
        hotel1.setName("Grand Hotel Minsk");
        hotel1.setAddress("пр-т Независимости, 15");
        hotel1.setCity("Минск");
        hotel1.setStars(5);
        hotel1.setDescription("Роскошный отель в центре Минска с видом на проспект");
        hotel1.setAvailable(true);
        hotelRepository.save(hotel1);

        Hotel hotel2 = new Hotel();
        hotel2.setName("Бизнес Отель");
        hotel2.setAddress("ул. Новый Арбат, 15");
        hotel2.setCity("Москва");
        hotel2.setStars(4);
        hotel2.setDescription("Удобный отель для деловых поездок");
        hotel2.setAvailable(true);
        hotelRepository.save(hotel2);

        Hotel hotel3 = new Hotel();
        hotel3.setName("Питер Отель");
        hotel3.setAddress("Невский проспект, 20");
        hotel3.setCity("Санкт-Петербург");
        hotel3.setStars(4);
        hotel3.setDescription("Отель в культурной столице");
        hotel3.setAvailable(true);
        hotelRepository.save(hotel3);
    }

    private void createRooms() {
        List<Amenity> amenities = amenityRepository.findAll();
        List<Hotel> hotels = hotelRepository.findAll();

        for (Hotel hotel : hotels) {
            for (int i = 1; i <= 5; i++) {
                Room room = new Room();
                room.setNumber(String.valueOf(100 + i));
                room.setFloor(1);
                room.setCapacity(2);
                room.setType(i % 2 == 0 ? "STANDARD" : "DELUXE");

                switch (hotel.getName()) {
                    case "Grand Hotel Minsk":
                        room.setPrice(150.0 + i * 30);
                        break;
                    case "Бизнес Отель":
                        room.setPrice(100.0 + i * 20);
                        break;
                    case "Питер Отель":
                        room.setPrice(90.0 + i * 25);
                        break;
                    default:
                        room.setPrice(80.0 + i * 15);
                }

                room.setAvailable(true);
                room.setHotel(hotel);

                Set<Amenity> amenitySet = new HashSet<>();
                if (i % 2 == 0) {
                    amenitySet.addAll(amenities.subList(0, Math.min(2, amenities.size())));
                } else {
                    amenitySet.addAll(amenities.subList(2, Math.min(4, amenities.size())));
                }
                room.setAmenities(amenitySet);

                roomRepository.save(room);
            }
        }
    }

    private void createGuests() {
        Guest guest1 = new Guest();
        guest1.setFirstName("Александра");
        guest1.setLastName("Лукашевич");
        guest1.setEmail("sasha@email.com");
        guest1.setPhone("+375-29-123-45-67");
        guest1.setRegistrationDate(LocalDate.now().minusMonths(1));
        guestRepository.save(guest1);

        Guest guest2 = new Guest();
        guest2.setFirstName("Иван");
        guest2.setLastName("Петров");
        guest2.setEmail("ivan.petrov@email.com");
        guest2.setPhone("+375-33-456-78-90");
        guest2.setRegistrationDate(LocalDate.now().minusWeeks(2));
        guestRepository.save(guest2);

        Guest guest3 = new Guest();
        guest3.setFirstName("Мария");
        guest3.setLastName("Иванова");
        guest3.setEmail("maria.ivanova@email.com");
        guest3.setPhone("+375-44-789-01-23");
        guest3.setRegistrationDate(LocalDate.now().minusDays(5));
        guestRepository.save(guest3);
    }

    private void createBookings() {
        List<Guest> guests = guestRepository.findAll();
        List<Room> rooms = roomRepository.findAll();

        if (!rooms.isEmpty() && !guests.isEmpty()) {

            Booking booking1 = new Booking();
            booking1.setCheckInDate(LocalDate.now().plusDays(10));
            booking1.setCheckOutDate(LocalDate.now().plusDays(15));
            booking1.setStatus("CONFIRMED");
            booking1.setRoom(rooms.get(0));
            booking1.setGuest(guests.get(0));

            long nights1 = ChronoUnit.DAYS.between(booking1.getCheckInDate(), booking1.getCheckOutDate());
            booking1.setTotalPrice(rooms.get(0).getPrice() * nights1);
            bookingRepository.save(booking1);

            if (rooms.size() > 1 && guests.size() > 1) {

                Booking booking2 = new Booking();
                booking2.setCheckInDate(LocalDate.now().plusDays(20));
                booking2.setCheckOutDate(LocalDate.now().plusDays(25));
                booking2.setStatus("CONFIRMED");
                booking2.setRoom(rooms.get(1));
                booking2.setGuest(guests.get(1));

                long nights2 = ChronoUnit.DAYS.between(booking2.getCheckInDate(), booking2.getCheckOutDate());
                booking2.setTotalPrice(rooms.get(1).getPrice() * nights2);
                bookingRepository.save(booking2);
            }

            if (rooms.size() > 2 && guests.size() > 2) {

                Booking booking3 = new Booking();
                booking3.setCheckInDate(LocalDate.now().plusDays(5));
                booking3.setCheckOutDate(LocalDate.now().plusDays(7));
                booking3.setStatus("PENDING");
                booking3.setRoom(rooms.get(2));
                booking3.setGuest(guests.get(2));

                long nights3 = ChronoUnit.DAYS.between(booking3.getCheckInDate(), booking3.getCheckOutDate());
                booking3.setTotalPrice(rooms.get(2).getPrice() * nights3);
                bookingRepository.save(booking3);
            }
        }
    }
}