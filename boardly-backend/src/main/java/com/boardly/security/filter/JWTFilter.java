package com.boardly.security.filter;

import com.boardly.common.dto.ApiErrorResponseDTO;
import com.boardly.exception.TokenExpiredException;
import com.boardly.security.service.CustomUserDetailsService;
import com.boardly.security.service.JWTFilterService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.UUID;


@Component
public class JWTFilter extends OncePerRequestFilter {
    private final JWTFilterService jwtFilterService;
    private final CustomUserDetailsService customUserService;

    public JWTFilter(JWTFilterService jwtFilterService, CustomUserDetailsService customUserService) {
        this.jwtFilterService = jwtFilterService;
        this.customUserService = customUserService;
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7);
            if (!token.isEmpty()) {
                try {
                    UUID uuId = jwtFilterService.validateToken(token).orElse(null);
                    if (uuId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                        UserDetails userDetails = customUserService.loadUserById(uuId);
                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                } catch (TokenExpiredException e) {
                    ApiErrorResponseDTO apiErrorResponseDTO = new ApiErrorResponseDTO();
                    apiErrorResponseDTO.setStatusCode(HttpServletResponse.SC_UNAUTHORIZED);
                    apiErrorResponseDTO.setTimestamp(Instant.now());
                    apiErrorResponseDTO.setMessage("Access Token is invalid or has expired");
                    response.setContentType("application/json");
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    ObjectMapper mapper = new ObjectMapper();
                    mapper.registerModule(new JavaTimeModule());
                    mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
                    mapper.writeValue(response.getOutputStream(), apiErrorResponseDTO);
                    return;
                }
            }
        }
        filterChain.doFilter(request, response);
    }
}
