package com.foodredistribution.foodredistribution.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.foodredistribution.foodredistribution.jwt.JwtAuthFilter;

@Configuration
@EnableMethodSecurity   // keeps @PreAuthorize working on controllers
public class SecurityConfig {

    @Autowired
    private JwtAuthFilter jwtAuthFilter;

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .cors(org.springframework.security.config.Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())

                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .authorizeHttpRequests(auth -> auth

                        // ── Public endpoints ─────────────────────────────
                        .requestMatchers(
                                "/api/v1/auth/**",
                                "/api/v1/health",
                                "/api/v1/food/available",
                                "/uploads/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/v3/api-docs",
                                "/hello"
                        ).permitAll()

                        // ── Admin-only management endpoints ──────────────
                        .requestMatchers("/api/v1/reports/admin/**").hasRole("ADMIN")

                        // ── All other endpoints: must be authenticated ────
                        // Admin can reach any endpoint not blocked above.
                        // Fine-grained role guards (DONOR / RECEIVER) are
                        // handled by @PreAuthorize on each controller method.
                        // Admin bypasses those via the @PreAuthorize expressions
                        // which include hasRole('ADMIN').
                        .anyRequest().authenticated()
                )

                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}