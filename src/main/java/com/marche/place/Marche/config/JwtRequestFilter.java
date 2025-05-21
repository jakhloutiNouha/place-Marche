package com.marche.place.Marche.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    @Autowired
    @Qualifier("userDetailsServiceImpl")  // Spécifier quelle implémentation utiliser
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        final String requestTokenHeader = request.getHeader("Authorization");

        // Logs pour le débogage
        System.out.println("=== JWT Filter Debug ===");
        System.out.println("Request URI: " + request.getRequestURI());
        System.out.println("Authorization Header: " + requestTokenHeader);

        String email = null;
        String jwtToken = null;

        if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
            jwtToken = requestTokenHeader.substring(7);
            System.out.println("Extracted JWT Token: " + jwtToken);

            try {
                email = jwtUtil.getEmailFromToken(jwtToken);
                System.out.println("Extracted email: " + email);
            } catch (IllegalArgumentException e) {
                System.out.println("Unable to get JWT Token: " + e.getMessage());
            } catch (ExpiredJwtException e) {
                System.out.println("JWT Token has expired: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("Error parsing JWT: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("No Bearer token found");
        }

        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(email);
                System.out.println("User details loaded: " + userDetails.getUsername());
                System.out.println("User authorities from UserDetails: " + userDetails.getAuthorities());

                if (jwtUtil.validateToken(jwtToken, userDetails)) {
                    System.out.println("Token validation successful");

                    // Extraire les autorités du token JWT
                    List<SimpleGrantedAuthority> authorities = new ArrayList<>();

                    try {
                        Claims claims = jwtUtil.getAllClaimsFromToken(jwtToken);
                        System.out.println("Token claims: " + claims);

                        // 1. Vérifier 'role'
                        if (claims.get("role") != null) {
                            String role = claims.get("role").toString();
                            System.out.println("Found role in token: " + role);

                            // Ajouter les autorités sous différentes formes pour assurer la compatibilité
                            authorities.add(new SimpleGrantedAuthority(role));

                            // Ajouter également avec préfixe ROLE_ si ce n'est pas déjà le cas
                            if (!role.startsWith("ROLE_")) {
                                authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
                            }
                        }

                        // 2. Vérifier 'authorities'
                        if (claims.get("authorities") != null) {
                            Object authClaim = claims.get("authorities");
                            System.out.println("Found authorities in token: " + authClaim);

                            if (authClaim instanceof List) {
                                @SuppressWarnings("unchecked")
                                List<String> authList = (List<String>) authClaim;
                                for (String auth : authList) {
                                    authorities.add(new SimpleGrantedAuthority(auth));
                                }
                            } else if (authClaim instanceof String) {
                                String auth = (String) authClaim;
                                authorities.add(new SimpleGrantedAuthority(auth));
                            }
                        }

                        // Si aucune autorité trouvée, utiliser celles du userDetails
                        if (authorities.isEmpty()) {
                            System.out.println("No authorities found in token, using userDetails authorities");
                            userDetails.getAuthorities().forEach(authority ->
                                    authorities.add(new SimpleGrantedAuthority(authority.getAuthority())));
                        }

                    } catch (Exception e) {
                        System.out.println("Error extracting authorities from token: " + e.getMessage());
                        // En cas d'erreur, utiliser les autorités de userDetails
                        userDetails.getAuthorities().forEach(authority ->
                                authorities.add(new SimpleGrantedAuthority(authority.getAuthority())));
                    }

                    System.out.println("Final authorities used for authentication: " + authorities);

                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userDetails, null, authorities);
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    System.out.println("Authentication set in SecurityContext");
                } else {
                    System.out.println("Token validation failed");
                }
            } catch (Exception e) {
                System.out.println("Error loading user details: " + e.getMessage());
                e.printStackTrace();
            }
        }
        System.out.println("=== End JWT Filter Debug ===");

        chain.doFilter(request, response);
    }
}