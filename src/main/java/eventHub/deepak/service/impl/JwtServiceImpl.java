package eventHub.deepak.service.impl;

import eventHub.deepak.entity.User;
import eventHub.deepak.enums.Role;
import eventHub.deepak.service.interfaces.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class JwtServiceImpl implements JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    private SecretKey getSigningKey(){
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
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
                        .get("userId",Long.class);
    }

    public Role extractRole(String token) {
        return
                extractAllClaims(token)
                        .get("role",Role.class);
    }

    public boolean validateToken(String jwt, UserDetails userDetails) {
        return extractEmail(jwt).equals(userDetails.getUsername())
                && !isTokenExpired(jwt);
    }

    private boolean isTokenExpired(String jwt) {
        return extractAllClaims(jwt).get(expiration).before(new Date());

    }
}
