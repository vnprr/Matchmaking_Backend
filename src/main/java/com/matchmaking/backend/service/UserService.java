package com.matchmaking.backend.service;

//3

import com.matchmaking.backend.model.Provider;
import com.matchmaking.backend.model.Role;
import com.matchmaking.backend.model.User;
import com.matchmaking.backend.model.UserProfile;
import com.matchmaking.backend.repository.UserRepository;
import com.matchmaking.backend.service.user.profile.UserProfileCreatorService;
import com.matchmaking.backend.service.user.profile.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final UserProfileCreatorService userProfileCreatorService;

    public User getUserByEmail(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    public boolean existsByEmail(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                user.isEnabled(),
                true,
                true,
                true,
                Collections.singletonList(new SimpleGrantedAuthority(user.getRole().name()))
        );
    }

    @Transactional
    public void saveUser(User user) {
        userRepository.save(user);
    }

    @Transactional
    public boolean verifyUser(String verificationCode) {
        Optional<User> userOpt = userRepository.findByVerificationCode(verificationCode);
        if (userOpt.isEmpty() || userOpt.get().isEnabled()) {
            return false;
        }
        User user = userOpt.get();
        user.setEnabled(true);
        user.setVerificationCode(null);
        return true;
    }

    @Transactional
    public User processOAuthPostLogin(String email, String firstName, String lastName) {
        return userRepository.findByEmail(email)
                .orElseGet(() -> {
                    // Tworzenie nowego użytkownika
                    User newUser = User.builder()
                            .email(email)
                            .enabled(true)
                            .role(Role.USER)
                            .provider(Provider.GOOGLE)
                            .password("AUTH_Google") // dla użytkowników OAuth
                            .failedLoginAttempts(0)
                            .build();

                    // Tworzenie profilu z inicjalizacją sekcji
                    UserProfile userProfile = userProfileCreatorService.createUserProfile(newUser, firstName, lastName);
                    newUser.setProfile(userProfile);

                    return userRepository.save(newUser);
                });
    }

    public boolean canResendVerificationEmail(User user) {
        if (user.getLastVerificationEmailSentAt() == null) {
            return true;
        }
        LocalDateTime threshold = user.getLastVerificationEmailSentAt().plusMinutes(15);
        return LocalDateTime.now().isAfter(threshold);
    }

    public Optional<User> findByVerificationCode(String code) {
        return userRepository.findByVerificationCode(code);
    }

}