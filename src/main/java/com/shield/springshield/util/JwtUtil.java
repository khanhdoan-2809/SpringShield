package com.shield.springshield.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j; // Use Lombok for logging
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;

@Slf4j
@Component
public class JwtUtil {

    private final SecretKey secretKey;
    private final long tokenValidity;

    public JwtUtil(@Value("${jwt.secret}") String secret,
                   @Value("${jwt.validity}") long tokenValidity) {
        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.tokenValidity = tokenValidity;
    }

    public String generateToken(String username) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .subject(username)
                .issuedAt(new java.util.Date(now))
                .expiration(new java.util.Date(now + tokenValidity))
                .signWith(secretKey)
                .compact();
    }
}