package com.boardly.security.service;

import com.boardly.exception.TokenExpiredException;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.*;

import javax.crypto.SecretKey;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Service
public class JWTFilterService {
    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.access_token_expiration}")
    private Long accessTokenExpirationTime; // In ms

    @Value("${jwt.refresh_token_expiration}")
    private Long refreshTokenExpirationTime; // In ms

    private static final SecureRandom secureRandom = new SecureRandom();


    private SecretKey secretKey;

    @PostConstruct
    public void init() {
        this.secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    public String generateToken(UUID userId) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(userId.toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(accessTokenExpirationTime)))
                .signWith(secretKey)
                .compact();
    }

    public String generateRefreshToken(UUID userId) {
        byte[] token = new byte[32]; // 32 bytes = 256 bits
        secureRandom.nextBytes(token);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(token);
    }

    public long getAccessTokenExpirationFromNow() {
        return Instant.now().plusMillis(accessTokenExpirationTime).getEpochSecond();
    }

    public Optional<UUID> validateToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return Optional.of(UUID.fromString(claims.getSubject()));
        } catch (ExpiredJwtException e) {
            throw new TokenExpiredException("Token has expired");
        } catch (JwtException e) {
            return Optional.empty();
        }
    }
}
