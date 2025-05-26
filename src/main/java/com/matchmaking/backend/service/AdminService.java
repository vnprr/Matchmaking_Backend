package com.matchmaking.backend.service;

import com.matchmaking.backend.model.Role;
import com.matchmaking.backend.model.User;
import com.matchmaking.backend.model.UserProfile;
import com.matchmaking.backend.model.AdminUserDTO;
import com.matchmaking.backend.model.AdminUserListDTO;
import com.matchmaking.backend.repository.UserRepository;
import com.matchmaking.backend.service.user.profile.UserProfileCreatorService;
import com.matchmaking.backend.service.user.profile.image.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CloudinaryService cloudinaryService;
    private final UserProfileCreatorService userProfileCreatorService;

    @Transactional(readOnly = true)
    public Page<AdminUserListDTO> getUsers(int page, int size, String search) {
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<User> usersPage;

        if (StringUtils.hasText(search)) {
            usersPage = userRepository.findByEmailContainingOrProfileFirstNameContainingOrProfileLastNameContaining(
                    search, search, search, pageRequest);
        } else {
            usersPage = userRepository.findAll(pageRequest);
        }

        return new PageImpl<>(
                usersPage.getContent().stream()
                        .map(this::mapUserToListDTO)
                        .collect(Collectors.toList()),
                pageRequest,
                usersPage.getTotalElements()
        );
    }

    @Transactional(readOnly = true)
    public AdminUserDTO getUserById(Long id) {
        User user = findUserById(id);
        return mapUserToDTO(user);
    }

    @Transactional
    public AdminUserDTO createUser(AdminUserDTO userDTO) {
        if (userRepository.findByEmail(userDTO.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Użytkownik z podanym adresem email już istnieje");
        }

        // Tworzenie nowego użytkownika
        User user = User.builder()
                .email(userDTO.getEmail())
                .password(passwordEncoder.encode(userDTO.getNewPassword()))
                .enabled(userDTO.isEnabled())
                .role(userDTO.getRole() != null ? userDTO.getRole() : Role.USER)
                .provider(userDTO.getProvider())
                .failedLoginAttempts(0)
                .build();

        // Tworzenie profilu z inicjalizacją sekcji
        UserProfile profile = userProfileCreatorService.createUserProfile(user, userDTO.getFirstName(), userDTO.getLastName());
        profile.setGender(userDTO.getGender());
        profile.setDateOfBirth(userDTO.getDateOfBirth());
        profile.setBio(userDTO.getBio());

        // Zapisanie użytkownika
        User savedUser = userRepository.save(user);
        return mapUserToDTO(savedUser);
    }

    @Transactional
    public AdminUserDTO updateUser(Long id, AdminUserDTO userDTO) {
        User user = findUserById(id);
        UserProfile profile = user.getProfile();

        // Aktualizacja danych użytkownika
        if (StringUtils.hasText(userDTO.getNewPassword())) {
            user.setPassword(passwordEncoder.encode(userDTO.getNewPassword()));
        }
        user.setEnabled(userDTO.isEnabled());
        user.setRole(userDTO.getRole());

        // Aktualizacja profilu
        profile.setFirstName(userDTO.getFirstName());
        profile.setLastName(userDTO.getLastName());
        profile.setGender(userDTO.getGender());
        profile.setDateOfBirth(userDTO.getDateOfBirth());
        profile.setBio(userDTO.getBio());

        User updatedUser = userRepository.save(user);
        return mapUserToDTO(updatedUser);
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = findUserById(id);

        // Usunięcie zdjęć z Cloudinary
        user.getProfile().getImages().forEach(image -> {
            try {
                cloudinaryService.deleteImage(image.getPublicId());
            } catch (Exception e) {
                // Logowanie błędu
            }
        });

        userRepository.delete(user);
    }

    @Transactional
    public void changeUserRole(Long id, Role role) {
        User user = findUserById(id);
        user.setRole(role);
        userRepository.save(user);
    }

    @Transactional
    public void changeUserStatus(Long id, boolean enabled) {
        User user = findUserById(id);
        user.setEnabled(enabled);
        userRepository.save(user);
    }

    private User findUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Użytkownik o podanym ID nie istnieje"));
    }

    private AdminUserListDTO mapUserToListDTO(User user) {
        AdminUserListDTO dto = new AdminUserListDTO();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setProfileId(user.getProfile() != null ? user.getProfile().getId() : null);
        dto.setFirstName(user.getProfile() != null ? user.getProfile().getFirstName() : null);
        dto.setLastName(user.getProfile().getLastName());
        dto.setRole(user.getRole());
        dto.setProvider(user.getProvider());
        dto.setEnabled(user.isEnabled());
        dto.setCreatedAt(user.getCreatedAt());
        return dto;
    }

    private AdminUserDTO mapUserToDTO(User user) {
        AdminUserDTO dto = new AdminUserDTO();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setEnabled(user.isEnabled());
        dto.setRole(user.getRole());
        dto.setProvider(user.getProvider());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        dto.setFailedLoginAttempts(user.getFailedLoginAttempts());
        dto.setAccountLockedUntil(user.getAccountLockedUntil());

        // Dane profilu
        if (user.getProfile() != null) {
            dto.setProfileId(user.getProfile().getId());
            dto.setFirstName(user.getProfile().getFirstName());
            dto.setLastName(user.getProfile().getLastName());
            dto.setGender(user.getProfile().getGender());
            dto.setDateOfBirth(user.getProfile().getDateOfBirth());
            dto.setBio(user.getProfile().getBio());
        }

        return dto;
    }
}