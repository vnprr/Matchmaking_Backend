package com.matchmaking.backend.service;

//3

import com.matchmaking.backend.model.Provider;
import com.matchmaking.backend.model.Role;
import com.matchmaking.backend.model.User;
import com.matchmaking.backend.model.UserProfile;
import com.matchmaking.backend.repository.UserRepository;
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

    public User getUserByEmail(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
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
                    User newUser = User.builder()
                            .email(email)
                            .enabled(true)
                            .role(Role.USER)
                            .provider(Provider.GOOGLE)
                            .password("AUTH_Google") // for OAuth users
                            .profile(new UserProfile(firstName, lastName))
                            .build();

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


}

// 2
//import com.matchmaking.backend.model.Provider;
//import com.matchmaking.backend.model.RegisterRequestDTO;
//import com.matchmaking.backend.model.Role;
//import com.matchmaking.backend.model.User;
//import com.matchmaking.backend.repository.UserRepository;
//import lombok.*;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.security.core.userdetails.UsernameNotFoundException;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.stereotype.Service;
//
//import java.util.Collections;
//import java.util.Optional;
//import java.util.UUID;
//
//@Service
//@RequiredArgsConstructor
//public class UserService implements UserDetailsService {
//
//    private final UserRepository userRepository;
//
//    @Override
//    public UserDetails loadUserByUsername(String email) {
//        User user = userRepository.findByEmail(email)
//                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
//        return new org.springframework.security.core.userdetails.User(
//                user.getEmail(), user.getPassword(), user.isEnabled(), true, true, true,
//                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
//    }
//
//    public User registerUser(RegisterRequestDTO request) {
//        User user = User.builder()
//                .email(request.getEmail())
//                .password(new BCryptPasswordEncoder().encode(request.getPassword()))
//                .enabled(false)
//                .role(Role.USER)
//                .provider(Provider.LOCAL)
//                .verificationCode(UUID.randomUUID().toString())
//                .build();
//        return userRepository.save(user);
//    }
//
//    public void enableUser(User user) {
//        user.setEnabled(true);
//        userRepository.save(user);
//    }
//
//    public User processOAuthPostLogin(String email, String firstName, String lastName) {
//        User user = userRepository.findByEmail(email).orElseGet(() ->
//                User.builder()
//                        .email(email)
//                        //.firstName(firstName)
//                        //.lastName(lastName)
//                        .enabled(true)
//                        .provider(Provider.GOOGLE)
//                        .build()
//        );
//        return userRepository.save(user);
//    }
//
//    public boolean existsByEmail(String email) {
//        return userRepository.findByEmail(email).isPresent();
//    }
//
//    public boolean verifyUser(String verificationCode) {
//        Optional<User> userOpt = userRepository.findByVerificationCode(verificationCode);
//
//        if (userOpt.isEmpty() || userOpt.get().isEnabled()) {
//            return false;
//        }
//
//        User user = userOpt.get();
//        user.setEnabled(true);
//        user.setVerificationCode(null);
//
//
//        return true;
//    }
//}


//package com.matchmaking.backend.service;
//
//import com.matchmaking.backend.model.*;
//import com.matchmaking.backend.repository.UserProfileRepository;
//import com.matchmaking.backend.repository.UserRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.security.core.userdetails.UsernameNotFoundException;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.Optional;
//import java.util.UUID;
//
//
//
//import java.util.List;
//import java.util.Optional;
//import java.util.UUID;
//@Service
//@RequiredArgsConstructor
//public class UserService implements UserDetailsService {
//
//    private final UserRepository userRepository;
//    private final PasswordEncoder passwordEncoder;
//    private final UserProfileRepository userProfileRepository;
//    private final MailService mailService;
//
//
//
//    @Override
//    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
//        User user = userRepository.findByEmail(email).orElseThrow(() ->
//                new UsernameNotFoundException("Nie znaleziono użytkownika o takim e-mailu"));
//        return new org.springframework.security.core.userdetails.User(
//                user.getEmail(), user.getPassword(), user.
//    }
//
//
//    @Transactional(readOnly = true)
//    public boolean existsByEmail(String email) {
//        return userRepository.findByEmail(email).isPresent();
//    }
//
//    @Transactional
//    public void registerUser(RegisterRequestDTO request) {
//        String hashedPassword = passwordEncoder.encode(request.getPassword());
//        String verificationCode = UUID.randomUUID().toString();
//
//        User user = new User();
//        user.setEmail(request.getEmail());
//        user.setPassword(hashedPassword);
//        user.setEnabled(false);
//        user.setVerificationCode(verificationCode);
//        user.setRole(Role.USER);
//
//        userRepository.save(user);
//        mailService.sendVerificationEmail(user.getEmail(), verificationCode);
//    }
//
//    @Transactional
//    public boolean verifyUser(String verificationCode) {
//        Optional<User> userOpt = userRepository.findByVerificationCode(verificationCode);
//
//        if (userOpt.isEmpty() || userOpt.get().isEnabled()) {
//            return false;
//        }
//
//        User user = userOpt.get();
//        user.setEnabled(true);
//        user.setVerificationCode(null);
//        createEmptyUserProfile(user);
//        userRepository.save(user);
//
//        return true;
//    }
//
//    @Transactional
//    public void createEmptyUserProfile(User user) {
//        UserProfile profile = new UserProfile();
//        profile.setUser(user);
//        user.setProfile(profile); // najpierw przypisz profil do usera
//        userProfileRepository.save(profile);
//    }
//
//
//}