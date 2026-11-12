package com.example.blog.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtils {

    private final SecretKey secretKey;
    private final long accessTokenExpiresIn;

    public JwtUtils(JwtProperties jwtProperties) {
        byte[] keyBytes = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
        this.accessTokenExpiresIn = jwtProperties.getAccessTokenExpiresIn();
    }

    public String generate(Long adminId, String username) {
        long now = System.currentTimeMillis();
        long exp = now + accessTokenExpiresIn;
        return Jwts.builder()
                .subject(username)
                .claim("adminId", adminId)
                .issuedAt(new Date(now))
                .expiration(new Date(exp))
                .signWith(secretKey)
                .compact();
    }

    public Claims parse(String token) {
        try {
            return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();
        } catch (Exception e) {
            return null;
        }
    }

    public Long getAdminId(String token) {
        Claims c = parse(token);
        if (c == null) return null;
        Object id = c.get("adminId");
        return id == null ? null : ((Number) id).longValue();
    }

    public String getUsername(String token) {
        Claims c = parse(token);
        return c == null ? null : c.getSubject();
    }
}
