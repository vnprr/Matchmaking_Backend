package com.matchmaking.backend.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.JwtException;

import java.util.Date;

@Service
public class JwtService {

    // Secret key for signing the JWT. In a real application, this should be stored securely.
    private final String SECRET_KEY = "securesecuresecuresecuresecuresecuresecuresecure";

    /**
     * Generuje token JWT dla podanego użytkownika.
     *
     * @param username nazwa użytkownika, dla którego generowany jest token
     * @return wygenenerowany token JWT
     */
    public String generateToken(String username) {
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 86400000))
                .signWith(Keys.hmacShaKeyFor(SECRET_KEY.getBytes()), Jwts.SIG.HS256)
                .compact();
    }

    /**
     * Ekstrahuje nazwę użytkownika z podanego tokenu JWT.
     *
     * @param token  token JWT
     * @return nazwa użytkownika zawarta w tokenie
     */
    public String extractUsername(String token) {
        return Jwts.parser()
                .setSigningKey(SECRET_KEY.getBytes())
                .build()
                .parseSignedClaims(token)
                .getBody()
                .getSubject();
    }

    /**
     * waliduje token JWT, sprawdzając jego poprawność
     *
     * @param token walidowany token JWT
     * @return <ul><li>`true` jeśli token jest <b>poprawny</b>,</li><li>`false` w przeciwnym wypadku.</li></ul>
     */
    public boolean validateToken(String token) {
        try {
            extractUsername(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }
}