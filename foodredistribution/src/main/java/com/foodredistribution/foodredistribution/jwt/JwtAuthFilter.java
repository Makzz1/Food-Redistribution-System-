package com.foodredistribution.foodredistribution.jwt;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.foodredistribution.foodredistribution.entity.User;
import com.foodredistribution.foodredistribution.repository.UserRepository;

import org.springframework.data.redis.core.StringRedisTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        String email;
        try {
            email = jwtService.extractEmail(token);
        } catch (Exception e) {
            // Token is malformed or expired
            writeError(response, HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired token");
            return;
        }

        User user = null;
        String cacheKey = "jwt_user::" + email;
        
        try {
            String cachedData = redisTemplate.opsForValue().get(cacheKey);
            if (cachedData != null) {
                user = objectMapper.readValue(cachedData, User.class);
            }
        } catch (Exception e) {
            // Ignore cache errors, fallback to DB
        }

        if (user == null) {
            user = userRepository.findByEmail(email).orElse(null);
            
            if (user != null) {
                try {
                    // Cache the user details for 5 minutes
                    String json = objectMapper.writeValueAsString(user);
                    redisTemplate.opsForValue().set(cacheKey, json, java.time.Duration.ofMinutes(5));
                } catch (Exception e) {
                    // Ignore cache write errors
                }
            }
        }

        if (user == null) {
            // Token was valid but user no longer exists in DB
            writeError(response, HttpServletResponse.SC_UNAUTHORIZED, "User not found");
            return;
        }

        // Block banned users from accessing any protected resource
        if (user.isBanned()) {
            writeError(response, HttpServletResponse.SC_FORBIDDEN,
                    "Your account has been banned. Please contact support.");
            return;
        }

        // Email verification gate: only for /api/v1/food/** endpoints
        if (request.getRequestURI().startsWith("/api/v1/food") && !user.getEmailVerified()) {
            writeError(response, HttpServletResponse.SC_FORBIDDEN, "Please verify your email first");
            return;
        }

        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(
                        email,
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
                );

        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authToken);

        filterChain.doFilter(request, response);
    }

    private void writeError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.getWriter().write("{\"message\":\"" + message + "\",\"status\":" + status + "}");
    }
}