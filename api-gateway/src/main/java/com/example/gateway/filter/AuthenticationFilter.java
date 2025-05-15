package com.example.gateway.filter;

import com.example.gateway.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class AuthenticationFilter implements GlobalFilter, Ordered {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        // Skip auth check for login/register endpoints
        if (path.contains("/auth/login") || path.contains("/auth/register") || path.contains("/auth/forgot-password")) {
            return chain.filter(exchange);
        }

        // Check for Authorization header
        if (!exchange.getRequest().getHeaders().containsKey("Authorization")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing Authorization Header");
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Authorization Header");
        }

        String token = authHeader.substring(7);

        if (!jwtUtil.isTokenValid(token)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or Expired Token");
        }

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -1; // Execute early in the filter chain
    }
} 