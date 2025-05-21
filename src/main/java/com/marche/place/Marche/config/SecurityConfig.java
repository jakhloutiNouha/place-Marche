package com.marche.place.Marche.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.access.channel.ChannelProcessingFilter;
import org.springframework.web.filter.CorsFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final JwtAuthEntryPoint jwtAuthEntryPoint;
    private final JwtRequestFilter jwtRequestFilter;
    private final CorsFilter corsFilter;

    public SecurityConfig(JwtAuthEntryPoint jwtAuthEntryPoint, JwtRequestFilter jwtRequestFilter, CorsFilter corsFilter) {
        this.jwtAuthEntryPoint = jwtAuthEntryPoint;
        this.jwtRequestFilter = jwtRequestFilter;
        this.corsFilter = corsFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.disable())
                .csrf(csrf -> csrf.disable())
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(jwtAuthEntryPoint)
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers(
                                "/api/auth/**",
                                "/api/categories",
                                "/api/products",
                                "/api/products/**",
                                "/api/stores",
                                "/api/stores/**",
                                "/api/vendor-requests",
                                "/api/promotions",
                                "/api/reviews/store/**",
                                "/api/reviews/product/**"
                        ).permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/vendor-requests").permitAll()

                        // Endpoints de reviews nécessitant une authentification
                        .requestMatchers(HttpMethod.POST, "/api/reviews").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/reviews/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/reviews/**").authenticated()

                        // Endpoints accessibles à tout utilisateur authentifié
                        .requestMatchers("/api/users/profile").authenticated()

                        // TEMPORAIRE: ouvrir l'accès pour tests - à supprimer en production
                        .requestMatchers("/api/users/**").permitAll()

                        // Vendor-specific endpoints
                        .requestMatchers("/api/products/vendor/**").hasAnyAuthority("VENDOR", "Vendor", "ROLE_VENDOR")
                        .requestMatchers("/api/stores/vendor/**").hasAnyAuthority("VENDOR", "Vendor", "ROLE_VENDOR")
                        .requestMatchers("/api/promotions/vendor").hasAnyAuthority("VENDOR", "Vendor", "ROLE_VENDOR")

                        // All other endpoints require authentication
                        .anyRequest().authenticated()
                );

        // Ajouter les filtres
        http.addFilterBefore(corsFilter, ChannelProcessingFilter.class);
        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}