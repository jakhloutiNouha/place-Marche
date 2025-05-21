package com.marche.place.Marche.config;

import com.marche.place.Marche.entity.User;
import com.marche.place.Marche.enums.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {

    @Value("${jwt.secret:mysecretkeymysecretkeymysecretkeymysecretkey}")
    private String secret;

    @Value("${jwt.expiration:86400000}") // 24 heures en millisecondes par défaut
    private long jwtExpirationInMs;

    // Génère un token pour un utilisateur
    public String generateToken(UserDetails userDetails, User user) {
        Map<String, Object> claims = new HashMap<>();

        // Ajouter le rôle explicitement dans les claims
        if (user != null && user.getRole() != null) {
            claims.put("role", user.getRole().toString());

            // Ajouter également comme autorité compatible avec Spring Security
            claims.put("authorities", user.getRole().toString());
        }

        return doGenerateToken(claims, userDetails.getUsername());
    }

    private String doGenerateToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private Key getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // Récupère l'email depuis le token
    public String getEmailFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    // Récupère la date d'expiration depuis le token
    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    // Pour récupérer toute information du token, nous avons besoin de la clé secrète
    public Claims getAllClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // Vérifie si le token a expiré
    private Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    // Valide le token
    public Boolean validateToken(String token, UserDetails userDetails) {
        try {
            final String email = getEmailFromToken(token);
            return (email.equals(userDetails.getUsername()) && !isTokenExpired(token));
        } catch (Exception e) {
            System.out.println("Erreur lors de la validation du token: " + e.getMessage());
            return false;
        }
    }

    // Récupère le rôle depuis le token
    public UserRole getRoleFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        String roleName = (String) claims.get("role");

        if (roleName != null) {
            try {
                return UserRole.valueOf(roleName);
            } catch (IllegalArgumentException e) {
                System.out.println("Rôle invalide dans le token: " + roleName);
            }
        }

        return null;
    }

}