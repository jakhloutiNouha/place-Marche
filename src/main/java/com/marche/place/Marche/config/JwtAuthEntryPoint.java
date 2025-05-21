package com.marche.place.Marche.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.Serializable;

@Component
public class JwtAuthEntryPoint implements AuthenticationEntryPoint, Serializable {

    private static final long serialVersionUID = -7858869558953243875L;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {

        // Log pour le débogage
        System.out.println("JWT Auth Entry Point triggered: " + authException.getMessage());
        System.out.println("Request URI: " + request.getRequestURI());
        System.out.println("Auth header: " + request.getHeader("Authorization"));

        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Non autorisé: " + authException.getMessage());
    }
}