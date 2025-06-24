package com.matchmaking.backend.service;

import com.matchmaking.backend.model.auth.*;
import com.matchmaking.backend.model.profile.UserProfile;
import com.matchmaking.backend.service.profile.UserProfileCreatorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final MailService mailService;
    private final PasswordEncoder passwordEncoder;
    private final UserProfileCreatorService userProfileCreatorService;


    @Transactional
    public ResponseEntity<?> register(RegisterRequestDTO request) {
        if (userService.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().body("Email jest już używany");
        }

        // Tworzenie nowego użytkownika
        User newUser = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .enabled(false)
                .verificationCode(UUID.randomUUID().toString())
                .role(Role.USER)
                .provider(Provider.LOCAL)
                .failedLoginAttempts(0)
                .build();

        // Tworzenie profilu użytkownika z inicjalizacją sekcji
        UserProfile userProfile = userProfileCreatorService.createUserProfile(newUser, null, null);
        newUser.setProfile(userProfile);

        // Zapisanie użytkownika
        userService.saveUser(newUser);

        // Wysłanie maila weryfikacyjnego
        mailService.sendVerificationEmail(newUser);

        return ResponseEntity.ok("Rejestracja przebiegła pomyślnie. Sprawdź email, aby zweryfikować konto.");
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
            User user = null;
            try {
                user = userService.getUserByEmail(request.getEmail());

                // Sprawdź czy konto jest zablokowane
                if (user.getAccountLockedUntil() != null && LocalDateTime.now().isBefore(user.getAccountLockedUntil())) {
                    long minutesLeft = java.time.Duration.between(LocalDateTime.now(), user.getAccountLockedUntil()).toMinutes() + 1;
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body("Konto zablokowane. Spróbuj ponownie za " + minutesLeft + " minut.");
                }

                if (user.getProvider() == Provider.GOOGLE) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body("Please use Google to log in");
                }
            } catch (UsernameNotFoundException ignored) {
                // Kontynuuj proces logowania - błędne dane i tak zostaną odrzucone
            }

            try {
                // Próba uwierzytelnienia
                Authentication authentication = authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                request.getEmail(),
                                request.getPassword()
                        )
                );

                // Uwierzytelnienie się powiodło
                UserDetails userDetails = (UserDetails) authentication.getPrincipal();
                user = userService.getUserByEmail(userDetails.getUsername());

                // Resetuj licznik nieudanych prób logowania
                if (user.getFailedLoginAttempts() > 0) {
                    user.setFailedLoginAttempts(0);
                    user.setAccountLockedUntil(null);
                    userService.saveUser(user);
                }

                if (!user.isEnabled()) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Account not verified");
                }

                String token = jwtService.generateToken(userDetails.getUsername());

                return ResponseEntity.ok(
                        new AuthResponseDTO(
                                token,
                                user.getEmail(),
                                user.getRole()));

            } catch (BadCredentialsException e) {
                if (user != null) {
                    // Uwierzytelnienie nie powiodło się - zwiększ licznik prób
                    user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);

                    // Jeśli przekroczono limit prób, zablokuj konto
                    if (user.getFailedLoginAttempts() >= 5) {
                        user.setAccountLockedUntil(LocalDateTime.now().plusMinutes(5));
                        userService.saveUser(user);
                        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                .body("Przekroczono limit nieudanych prób logowania. Konto zablokowane na 5 minut.");
                    }

                    userService.saveUser(user);
                }
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid email or password");
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error");
        }
    }

    @Transactional
    public ResponseEntity<?> changePassword(PasswordChangeDTO request) {
        // Pobierz dane zalogowanego użytkownika z kontekstu bezpieczeństwa
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User user = userService.getUserByEmail(email);

        // Sprawdź czy użytkownik nie jest z OAuth2
        if (user.getProvider() != Provider.LOCAL) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Zmiana hasła jest dostępna tylko dla kont lokalnych");
        }

        try {
            // Sprawdź obecne hasło przez ponowne uwierzytelnienie
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, request.getCurrentPassword())
            );

            // Sprawdź czy nowe hasło i potwierdzenie są identyczne
            if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
                return ResponseEntity.badRequest().body("Nowe hasło i potwierdzenie nie są takie same");
            }

            // Zmiana hasła
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
            userService.saveUser(user);

            return ResponseEntity.ok("Hasło zostało zmienione pomyślnie");

        } catch (BadCredentialsException e) {
            return ResponseEntity.badRequest().body("Obecne hasło jest niepoprawne");
        }
    }

    @Transactional
    public ResponseEntity<?> changeEmail(EmailChangeDTO request) {
        // Pobierz dane zalogowanego użytkownika z kontekstu bezpieczeństwa
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentEmail = authentication.getName();
        User user = userService.getUserByEmail(currentEmail);

        // Sprawdź czy użytkownik nie jest z OAuth2
        if (user.getProvider() != Provider.LOCAL) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Zmiana adresu email jest dostępna tylko dla kont lokalnych");
        }

        try {
            // Sprawdź hasło przez ponowne uwierzytelnienie
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(currentEmail, request.getPassword())
            );

            // Sprawdź czy nowy email jest już w użyciu
            if (userService.existsByEmail(request.getNewEmail())) {
                return ResponseEntity.badRequest().body("Podany adres email jest już używany");
            }

            // Zmiana adresu email
            String oldEmail = user.getEmail();
            user.setEmail(request.getNewEmail());
            userService.saveUser(user);

            // Wysłanie powiadomień
            mailService.sendEmailChangeNotification(oldEmail, request.getNewEmail());

            return ResponseEntity.ok("Adres email został zmieniony pomyślnie");

        } catch (BadCredentialsException e) {
            return ResponseEntity.badRequest().body("Podane hasło jest niepoprawne");
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

    @Transactional
    public ResponseEntity<?> requestPasswordReset(String email) {
        try {
            User user = userService.getUserByEmail(email);

            if (user.getProvider() != Provider.LOCAL) {
                return ResponseEntity.badRequest().body("To konto używa logowania przez " + user.getProvider() +
                        ". Nie można zresetować hasła.");
            }

            String resetToken = UUID.randomUUID().toString();
            user.setVerificationCode(resetToken);
            user.setResetTokenExpiration(LocalDateTime.now().plusHours(1)); // Token ważny przez 1 godzinę
            user.setLastVerificationEmailSentAt(LocalDateTime.now());
            userService.saveUser(user);

            mailService.sendPasswordResetEmail(user);

            return ResponseEntity.ok("Email z instrukcją resetowania hasła został wysłany");
        } catch (UsernameNotFoundException ex) {
            return ResponseEntity.ok("Jeśli konto istnieje, email z instrukcją resetowania hasła został wysłany");
        }
    }

    @Transactional
    public ResponseEntity<?> resetPassword(String token, String newPassword) {
        Optional<User> userOpt = userService.findByVerificationCode(token);

        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Nieprawidłowy token resetowania hasła");
        }

        User user = userOpt.get();

        if (user.getResetTokenExpiration() == null || user.getResetTokenExpiration().isBefore(LocalDateTime.now())) {
            return ResponseEntity.badRequest().body("Token resetowania hasła wygasł");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setVerificationCode(null);
        user.setResetTokenExpiration(null); // Wyczyszczenie daty wygaśnięcia
        userService.saveUser(user);

        return ResponseEntity.ok("Hasło zostało pomyślnie zmienione");
    }
}