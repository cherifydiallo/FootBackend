package com.kratos.footbackend.service;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.function.Function;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.JwtParserBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private int jwtExpirationMs;
    
    @Value("${jwt.refreshexpiration}")
    private int refreshTokenExpirationMs;
    

    private SecretKey key;

    // Initializes the key after the class is instantiated and the jwtSecret is injected,
    // preventing the repeated creation of the key and enhancing performance
    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    // Generate JWT token - utilise identifiant
    public String generateToken(String identifiant) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        JwtBuilder builder = Jwts.builder();
        builder.setSubject(identifiant);  // Stocke l'identifiant dans le subject
        builder.setIssuedAt(now);
        builder.setExpiration(expiryDate);
        builder.signWith(this.key, SignatureAlgorithm.HS256);
        return builder.compact();
    }

    
 // Génère un refresh token basé sur l'identifiant
    public String generateRefreshToken(String identifiant) {
        Date now = new Date();
        Date refreshExpiryDate = new Date(now.getTime() + refreshTokenExpirationMs); 

        JwtBuilder builder = Jwts.builder();
        builder.setSubject(identifiant); // Stocke l'identifiant
        builder.setIssuedAt(now);
        builder.setExpiration(refreshExpiryDate);
        builder.signWith(this.key, SignatureAlgorithm.HS256);

        return builder.compact();
    }

    
    // Get identifiant from JWT token
    public String getIdentifiantFromToken(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Alias pour compatibilité Spring Security
    public String getUsernameFromToken(String token) {
        return getIdentifiantFromToken(token);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        JwtParserBuilder parserBuilder = Jwts.parserBuilder();
        parserBuilder.setSigningKey(key);
        JwtParser parser = parserBuilder.build();
        return parser.parseClaimsJws(token).getBody();
    }

    // Validation du token avec UserDetails - CORRIGÉ
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String identifiant = getIdentifiantFromToken(token);
        return (identifiant.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    // Méthode pour vérifier l'expiration - AJOUTÉE
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // Extraire la date d'expiration - AJOUTÉE
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public long getExpirationTime() {
        return jwtExpirationMs;
    }

    // Validate JWT token structure et signature
    public boolean validateJwtToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (SecurityException e) {
            System.out.println("Invalid JWT signature: " + e.getMessage());
        } catch (MalformedJwtException e) {
            System.out.println("Invalid JWT token: " + e.getMessage());
        } catch (ExpiredJwtException e) {
            System.out.println("JWT token is expired: " + e.getMessage());
        } catch (UnsupportedJwtException e) {
            System.out.println("JWT token is unsupported: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("JWT claims string is empty: " + e.getMessage());
        }
        return false;
    }
}