package com.matchmaking.backend.controller;


import com.matchmaking.backend.model.LoginRequestDTO;
import com.matchmaking.backend.model.RegisterRequestDTO;
import com.matchmaking.backend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequestDTO request) {
        return authService.register(request);
    }

    @GetMapping("/verify")
    public ResponseEntity<?> verify(@RequestParam String code) {
        return authService.verifyUser(code);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDTO request) {
        return authService.login(request);
    }

    @PostMapping("/resend-verification-email")
    public ResponseEntity<?> resendVerificationEmail(@RequestParam String email) {
        return authService.resendVerificationEmail(email);
    }

}

//import com.matchmaking.backend.model.LoginRequestDTO;
//import com.matchmaking.backend.model.RegisterRequestDTO;
//import com.matchmaking.backend.model.User;
//import com.matchmaking.backend.service.JwtService;
//import com.matchmaking.backend.service.MailService;
//import com.matchmaking.backend.service.UserService;
//import jakarta.validation.Valid;
//import lombok.RequiredArgsConstructor;
//import org.springframework.context.annotation.Bean;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.authentication.ProviderManager;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.AuthenticationException;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.Map;
//
//@RestController
//@RequestMapping("/api/auth")
//@RequiredArgsConstructor
//public class AuthController {
//
//    private final UserService userService;
//    private final MailService mailService;
//    private final JwtService jwtService;
//    private final AuthenticationManager authenticationManager; // Wstrzyknięty manager uwierzytelniania
//
//    @PostMapping("/register")
//    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequestDTO request) {
//        if (userService.existsByEmail(request.getEmail())) {
//            return ResponseEntity.badRequest().body("Email already in use");
//        }
//        User user = userService.registerUser(request);
//        mailService.sendVerificationEmail(user);
//        return ResponseEntity.ok("Check your email to confirm registration");
//    }
//
//    @GetMapping("/verify")
//    public ResponseEntity<?> verify(@RequestParam String code) {
//        boolean success = userService.verifyUser(code);
//        return success ?
//                ResponseEntity.ok("Account verified successfully") :
//                ResponseEntity.badRequest().body("Invalid or expired verification code");
//    }
//
//    @PostMapping("/login")
//    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDTO request) {
//        try {
//            Authentication authentication = authenticationManager.authenticate(
//                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
//            );
//            String token = jwtService.generateToken(authentication.getName());
//            return ResponseEntity.ok(Map.of("token", token));
//        } catch (AuthenticationException ex) {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
//        }
//    }
//}



//package com.matchmaking.backend.controller;
//
//import com.matchmaking.backend.model.AuthResponseDTO;
//import com.matchmaking.backend.model.LoginRequestDTO;
//import com.matchmaking.backend.model.RegisterRequestDTO;
//import com.matchmaking.backend.model.Role;
//import com.matchmaking.backend.service.UserService;
//import jakarta.validation.Valid;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.authentication.BadCredentialsException;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.core.userdetails.UsernameNotFoundException;
//import org.springframework.validation.BindingResult;
//import org.springframework.web.bind.annotation.*;
//import com.matchmaking.backend.config.JwtUtil;
//
//import java.util.Map;
//import java.util.List;
//import java.util.stream.Collectors;
//
//@RestController
//@RequestMapping("/api/auth")
//@RequiredArgsConstructor
//public class AuthController {
//
//    private static final String EMAIL_ALREADY_EXISTS_MESSAGE = "Konto z podanym adresem email już istnieje.";
//    private static final String REGISTER_SUCCESS_MESSAGE = "Użytkownik został pomyślnie zarejestrowany, sprawdź swój adres email, aby aktywować konto.";
//    private static final String VERIFY_SUCCESS_MESSAGE = "Konto jest teraz aktywne!";
//    private static final String VERIFY_FAILURE_MESSAGE = "Niepoprawny lub przedawniony kod weryfikacyjny.";
//
//    private final UserService userService;
//    private final AuthenticationManager authenticationManager;
//    private final JwtUtil jwtUtil;
//
//    @PostMapping("/register")
//    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequestDTO registerRequest,
//                                          BindingResult bindingResult) {
//        if (hasValidationErrors(bindingResult)) {
//            return validationErrorResponse(bindingResult);
//        }
//
//        if (emailAlreadyExists(registerRequest.getEmail())) {
//            return badRequestResponse(EMAIL_ALREADY_EXISTS_MESSAGE);
//        }
//
//        userService.registerUser(registerRequest);
//        return ResponseEntity.ok(REGISTER_SUCCESS_MESSAGE);
//    }
//
//    @GetMapping("/verify")
//    public ResponseEntity<?> verifyUser(@RequestParam("code") String verificationCode) {
//        return userService.verifyUser(verificationCode)
//                ? ResponseEntity.ok(VERIFY_SUCCESS_MESSAGE)
//                : badRequestResponse(VERIFY_FAILURE_MESSAGE);
//    }
//
//    @PostMapping("/login")
//    public ResponseEntity<?> login(
//            @RequestBody LoginRequestDTO request
//    ) {
//        try {
//            Authentication authentication = authenticationManager
//                    .authenticate(
//                    new UsernamePasswordAuthenticationToken(
//                            request.getEmail(),
//                            request.getPassword())
//            );
//
//            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
//            String jwt = jwtUtil.generateToken(userDetails);
//
//            String roleString = userDetails
//                    .getAuthorities()
//                    .stream()
//                    .findFirst()
//                    .map(auth -> auth.getAuthority().replace("ROLE_", ""))
//                    .orElse("USER");
//
//            Role role = Role.valueOf(roleString);
//
//            return ResponseEntity.ok(new AuthResponseDTO(
//                    jwt,
//                    userDetails.getUsername(),
//                    role)
//            );
//        } catch (UsernameNotFoundException ex) {
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Użytkownik nie istnieje."));
//        } catch (BadCredentialsException ex) {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Niepoprawne hasło."));
//        } catch (Exception ex) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Wystąpił nieoczekiwany błąd."));
//        }
//    }
//
//
//    private boolean hasValidationErrors(BindingResult bindingResult) {
//        return bindingResult.hasErrors();
//    }
//
//    private ResponseEntity<?> validationErrorResponse(BindingResult bindingResult) {
//        List<String> errors = bindingResult.getFieldErrors()
//                .stream()
//                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
//                .collect(Collectors.toList());
//
//        return ResponseEntity.badRequest().body(errors);
//    }
//
//    private boolean emailAlreadyExists(String email) {
//        return userService.existsByEmail(email);
//    }
//
//    private ResponseEntity<?> badRequestResponse(String message) {
//        return ResponseEntity.badRequest().body(message);
//    }
//}