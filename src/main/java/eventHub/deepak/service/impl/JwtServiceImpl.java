package eventHub.deepak.service.impl;

import eventHub.deepak.entity.User;
import eventHub.deepak.enums.Role;
import eventHub.deepak.service.interfaces.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import jakarta.annotation.PostConstruct;

@Service
public class JwtServiceImpl implements JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    @PostConstruct
    void validateConfiguration() {
        if (secret == null || secret.getBytes(java.nio.charset.StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("JWT_SECRET must contain at least 32 UTF-8 bytes");
        }
        if (expiration <= 0) {
            throw new IllegalStateException("JWT_EXPIRATION_MS must be positive");
        }
    }

    private SecretKey getSigningKey(){
        return Keys.hmacShaKeyFor(secret.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    public String generateToken(User user){
        return Jwts.builder()
                .subject(user.getEmail())
                .claim("userId",String.valueOf(user.getId()))
                .claim("role",user.getRole().name())
                .issuedAt(new Date())
                .expiration(
                        new Date(
                                System.currentTimeMillis() + expiration
                        )
                )
                .signWith(getSigningKey())
                .compact();
    }
    private Claims extractAllClaims(
            String token
    ){
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extractEmail(String token){
        return extractAllClaims(token).getSubject();
    }

    public Long extractUserId(String token) {
        return
                extractAllClaims(token)
                .get("userId", Number.class).longValue();
    }

    public Role extractRole(String token) {
        String role = extractAllClaims(token).get("role", String.class);
        return role == null ? null : Role.valueOf(role);
    }

    public boolean validateToken(String jwt, UserDetails userDetails) {
        return extractEmail(jwt).equals(userDetails.getUsername())
                && !isTokenExpired(jwt);
    }

    private boolean isTokenExpired(String jwt) {
        return extractAllClaims(jwt).getExpiration().before(new Date());

    }
}
