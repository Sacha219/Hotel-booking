package com.example.hotelbooking.service;

import com.example.hotelbooking.dto.AuthLoginRequestDTO;
import com.example.hotelbooking.dto.AuthRegisterRequestDTO;
import com.example.hotelbooking.dto.AuthResponseDTO;
import com.example.hotelbooking.entity.AppUser;
import com.example.hotelbooking.entity.Guest;
import com.example.hotelbooking.entity.UserRole;
import com.example.hotelbooking.repository.AppUserRepository;
import com.example.hotelbooking.repository.GuestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AppUserRepository appUserRepository;
    private final GuestRepository guestRepository;

    @Transactional
    public AuthResponseDTO register(AuthRegisterRequestDTO request) {
        String email = request.getEmail().trim().toLowerCase();
        if (appUserRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException("Пользователь с такой почтой уже существует");
        }

        UserRole role = request.getRole() == null ? UserRole.USER : request.getRole();
        AppUser user = new AppUser();
        user.setFirstName(request.getFirstName().trim());
        user.setLastName(normalizeBlank(request.getLastName()) == null ? "" : request.getLastName().trim());
        user.setEmail(email);
        user.setPhone(normalizeBlank(request.getPhone()));
        user.setPassword(request.getPassword());
        user.setRole(role);

        if (role == UserRole.USER) {
            guestRepository.findByEmailIgnoreCase(email)
                    .map(Guest::getId)
                    .ifPresent(user::setGuestId);
        }

        return toResponse(appUserRepository.save(user));
    }

    @Transactional(readOnly = true)
    public AuthResponseDTO login(AuthLoginRequestDTO request) {
        AppUser user = appUserRepository.findByEmailIgnoreCase(request.getEmail().trim())
                .orElseThrow(() -> new IllegalArgumentException("Неверная почта или пароль"));

        if (!user.getPassword().equals(request.getPassword())) {
            throw new IllegalArgumentException("Неверная почта или пароль");
        }

        return toResponse(user);
    }

    private AuthResponseDTO toResponse(AppUser user) {
        return new AuthResponseDTO(
                user.getId(),
                user.getRole(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPhone(),
                user.getGuestId()
        );
    }

    private String normalizeBlank(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }
}
