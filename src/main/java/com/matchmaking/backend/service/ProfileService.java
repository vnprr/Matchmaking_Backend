package com.matchmaking.backend.service;

import com.matchmaking.backend.model.*;
import com.matchmaking.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true) // transakcja typu READ-ONLY na pobieranie danych
    public ProfileResponseDTO getUserProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Nie znaleziono użytkownika o emailu: " + email));

        UserProfile profile = user.getProfile();

        return new ProfileResponseDTO(
                profile.getId(),
                profile.getFirstName(),
                profile.getLastName(),
                profile.getGender(),
                profile.getDateOfBirth()
        );
    }

    @Transactional // domyślnie propagacja REQUIRED
    public void updateUserProfile(String email, ProfileUpdateRequestDTO requestedProfile) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Nie znaleziono użytkownika o emailu: " + email));
        UserProfile profile = user.getProfile();

        profile.setFirstName(requestedProfile.getFirstName());
        profile.setLastName(requestedProfile.getLastName());
        profile.setGender(requestedProfile.getGender());
        profile.setDateOfBirth(requestedProfile.getDateOfBirth());

        userRepository.save(user); // zapis powiązanego profilu przez kaskadę
    }
}