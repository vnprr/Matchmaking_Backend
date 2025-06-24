package com.matchmaking.backend.config;

import com.matchmaking.backend.model.auth.User;
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

        // token w odpowiedzi:
        response.sendRedirect("http://localhost:5173/oauth-callback?token=" + token);
    }
}