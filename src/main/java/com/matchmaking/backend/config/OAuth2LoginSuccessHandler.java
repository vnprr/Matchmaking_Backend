package com.matchmaking.backend.config;

import com.matchmaking.backend.model.User;
import com.matchmaking.backend.service.JwtService;
import com.matchmaking.backend.service.UserService;
import io.jsonwebtoken.io.IOException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;



@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final UserService userService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, jakarta.servlet.ServletException, java.io.IOException {

        DefaultOAuth2User oauthUser = (DefaultOAuth2User) authentication.getPrincipal();
        String email = oauthUser.getAttribute("email");
        String firstName = oauthUser.getAttribute("given_name");
        String lastName = oauthUser.getAttribute("family_name");

        User user = userService.processOAuthPostLogin(email, firstName, lastName);
        String token = jwtService.generateToken(user.getEmail());

        // Ustaw token w odpowiedzi albo przekieruj, zależnie od strategii frontendowej
        response.sendRedirect("http://localhost:5173/oauth-callback?token=" + token);
    }
}

//import java.io.IOException;
//import java.util.List;
//
//import com.matchmaking.backend.model.Role;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
//import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
//import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
//import org.springframework.stereotype.Component;
//
//import com.matchmaking.backend.config.JwtUtil;
//import com.matchmaking.backend.model.User;
//import com.matchmaking.backend.model.UserProfile;
//import com.matchmaking.backend.repository.UserRepository;
//import com.matchmaking.backend.repository.UserProfileRepository;
//
//@Component
//public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {
//
//    @Autowired
//    private JwtUtil jwtUtil;
//    @Autowired private UserRepository userRepo;
//    @Autowired private UserProfileRepository profileRepo;
//
//    @Override
//    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
//                                        Authentication authentication) throws IOException, ServletException {
//        // Pobierz dane użytkownika zalogowanego przez OAuth2
//        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
//        DefaultOAuth2User oauthUser = (DefaultOAuth2User) oauthToken.getPrincipal();
//        String email = oauthUser.getAttribute("email");
//        // Sprawdź czy użytkownik istnieje w bazie; jeśli nie, załóż konto
//        User user = userRepo.findByEmail(email).orElseGet(() -> {
//            User newUser = new User();
//            newUser.setEmail(email);
//            newUser.setPassword(""); // brak hasła (logowanie tylko przez OAuth)
//            newUser.setEnabled(true); // od razu aktywujemy, bo przez Google mamy zweryfikowany email
//            newUser.setRole(Role.USER);
//            // Ustaw profil z podstawowymi danymi z Google (imię, nazwisko)
//            UserProfile profile = new UserProfile();
//            profile.setFirstName(oauthUser.getAttribute("given_name"));
//            profile.setLastName(oauthUser.getAttribute("family_name"));
//            profile.setUser(newUser);
//            newUser.setProfile(profile);
//            return userRepo.save(newUser);
//        });
//        // Wygeneruj JWT dla tego użytkownika (na podstawie emaila/roli z bazy)
//        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
//                user.getEmail(), user.getPassword(),
//                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole())));
//        String jwt = jwtUtil.generateToken(userDetails);
//        // **Przekierowanie na frontend z tokenem**:
//        String redirectUrl = "http://localhost:5173/oauth2redirect?token=" + jwt;
//        response.sendRedirect(redirectUrl);
//    }
//}