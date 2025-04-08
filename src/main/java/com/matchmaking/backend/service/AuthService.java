package com.matchmaking.backend.service;

import com.matchmaking.backend.model.*;
import com.matchmaking.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final MailService mailService;
    private final PasswordEncoder passwordEncoder;

    public ResponseEntity<?> register(RegisterRequestDTO request) {
        if (userService.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().body("Email already in use");
        }

        User newUser = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .enabled(false)
                .verificationCode(UUID.randomUUID().toString())
                .role(Role.USER) // Default role
                .provider(Provider.LOCAL) // Default provider
                .profile(new UserProfile())
                .build();

        userService.saveUser(newUser);
        mailService.sendVerificationEmail(newUser);

        return ResponseEntity.ok("Registration successful, check your email for verification.");
    }

    public ResponseEntity<?> verifyUser(String verificationCode) {
        if (userService.verifyUser(verificationCode)) {
            return ResponseEntity.ok("Account activated successfully.");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Activation failed, invalid or expired code.");
        }
    }

    public ResponseEntity<?> login(LoginRequestDTO request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User user = userService.getUserByEmail(userDetails.getUsername());

            if (!user.isEnabled()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Account not verified");
            }

            String token = jwtService.generateToken(userDetails.getUsername());

            return ResponseEntity.ok(
                    new AuthResponseDTO(
                            token,
                            user.getEmail(),
                            user.getRole()));

        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid email or password");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error");
        }
    }

    @Transactional
    public ResponseEntity<?> resendVerificationEmail(String email) {
        User user;
        try {
            user = userService.getUserByEmail(email);
        } catch (UsernameNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
        }

        if (user.isEnabled()) {
            return ResponseEntity.badRequest().body("Account already verified.");
        }

        if (!userService.canResendVerificationEmail(user)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("You need to wait 15 minutes before sending a new verification email.");
        }

        user.setVerificationCode(UUID.randomUUID().toString());
        user.setLastVerificationEmailSentAt(LocalDateTime.now());
        userService.saveUser(user);

        mailService.sendVerificationEmail(user);

        return ResponseEntity.ok("Verification email resent successfully. Please check your inbox.");
    }
}
//
//import com.matchmaking.backend.model.*;
//import com.matchmaking.backend.repository.UserRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.core.userdetails.UsernameNotFoundException;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.LocalDateTime;
//import java.util.UUID;
//
//@Service
//@RequiredArgsConstructor
//public class AuthService {
//
//    private final UserService userService;
//    private final JwtService jwtService;
//    private final AuthenticationManager authenticationManager;
//    private final MailService mailService;
//    private final PasswordEncoder passwordEncoder;
//
//    public ResponseEntity<?> register(RegisterRequestDTO request) {
//        if (userService.existsByEmail(request.getEmail())) {
//            return ResponseEntity.badRequest().body("Email already in use");
//        }
//
//        User newUser = User.builder()
//                .email(request.getEmail())
//                .password(passwordEncoder.encode(request.getPassword()))
//                .enabled(false)
//                .verificationCode(UUID.randomUUID().toString())
//                .role(Role.USER) // ewentualnie domyślny np Role.USER
//                .provider(Provider.LOCAL) // ewentualnie domyślny lokalnie
//                .profile(new UserProfile())
//                .build();
//
//        userService.saveUser(newUser);
//        mailService.sendVerificationEmail(newUser);
//
//        return ResponseEntity.ok("Registration successful, check your email for verification.");
//    }
//
//    public ResponseEntity<?> verifyUser(String verificationCode) {
//        if (userService.verifyUser(verificationCode)) {
//            return ResponseEntity.ok("Account activated successfully.");
//        } else {
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Activation failed, invalid or expired code.");
//        }
//    }
//
//    public ResponseEntity<?> login(LoginRequestDTO request) {
//        try {
//            Authentication authentication = authenticationManager.authenticate(
//                    new UsernamePasswordAuthenticationToken(
//                            request.getEmail(),
//                            request.getPassword()
//                    )
//            );
//
//            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
//            User user = userService.getUserByEmail(userDetails.getUsername()); // Pobierz pełny obiekt użytkownika
//
//            if (!user.isEnabled()) {
//                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Account not verified");
//            }
//
//            String token = jwtService.generateToken(userDetails.getUsername());
//
//            return ResponseEntity.ok(
//                    new AuthResponseDTO(
//                            token,
//                            user.getEmail(),
//                            user.getRole()));
//
//        } catch (UsernameNotFoundException e) {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid email or password");
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error");
//        }
//    }
//
//    @Transactional
//    public ResponseEntity<?> resendVerificationEmail(String email) {
//        User user;
//        try {
//            user = userService.getUserByEmail(email);
//        } catch (UsernameNotFoundException ex) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
//        }
//
//        if (user.isEnabled()) {
//            return ResponseEntity.badRequest().body("Account already verified.");
//        }
//
//        if (!userService.canResendVerificationEmail(user)) {
//            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
//                    .body("You need to wait 15 minutes before sending a new verification email.");
//        }
//
//        // Wygeneruj nowy kod weryfikacyjny
//        user.setVerificationCode(UUID.randomUUID().toString());
//        user.setLastVerificationEmailSentAt(LocalDateTime.now());
//        userService.saveUser(user);
//
//        mailService.sendVerificationEmail(user);
//
//        return ResponseEntity.ok("Verification email resent successfully. Please check your inbox.");
//    }
//}