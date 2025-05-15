package com.example.enrollment.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;

import org.springframework.security.core.Authentication;
import jakarta.servlet.http.HttpServletRequest;

import java.security.Key;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Component
public class JwtAuthConverter implements Converter<String, Authentication> {

    @Value("${app.jwtSecret}")
    private String jwtSecret;

    @Override
    public Authentication convert(String token) {
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        
        Claims claims = extractAllClaims(token);
        
        String username = claims.getSubject();
        List<GrantedAuthority> authorities = new ArrayList<>();
        
        if (claims.get("role") != null) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + claims.get("role").toString()));
        }
        
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                username, null, authorities);
        
        return authToken;
    }
    
    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    private Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
    
    public Authentication getAuthentication(String token, HttpServletRequest request) {
        Authentication authentication = convert(token);
        
        if (authentication != null) {
            ((UsernamePasswordAuthenticationToken) authentication)
                    .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        }
        
        return authentication;
    }
} 