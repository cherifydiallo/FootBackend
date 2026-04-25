package com.kratos.footbackend.security;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.kratos.footbackend.service.JwtService;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtService jwtService, UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            String identifiant = null;

            try {
                identifiant = jwtService.getIdentifiantFromToken(token);
            } catch (ExpiredJwtException e) {
                logger.warn("Token expired for request, attempting refresh");
                // Token is expired, try to extract identifiant anyway
                try {
                    identifiant = e.getClaims().getSubject();
                } catch (Exception ex) {
                    logger.warn("Could not extract identifiant from expired token");
                }
            } catch (Exception e) {
                logger.warn("Error extracting identifiant from token: {}", e.getMessage());
            }

            if (identifiant != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                try {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(identifiant);
                    
                    if (jwtService.isTokenValid(token, userDetails)) {
                        // Token is valid, set authentication
                        UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        logger.info("User authenticated with valid token: {}", identifiant);
                    } else {
                        // Token is expired, try to refresh
                        logger.warn("Token is expired for user: {}, attempting to refresh", identifiant);
                        String newToken = jwtService.generateToken(identifiant);
                        
                        // Set the new token in response header
                        response.setHeader("X-New-Token", newToken);
                        response.setHeader("X-Token-Refreshed", "true");
                        
                        // Authenticate with new token
                        UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        logger.info("Token refreshed and new token generated for user: {}", identifiant);
                    }
                } catch (Exception e) {
                    logger.warn("Error during token validation or refresh for user: {}", identifiant);
                    // Token refresh failed, logout user (clear authentication context)
                    SecurityContextHolder.clearContext();
                    response.setHeader("X-Token-Expired", "true");
                    logger.warn("User logged out due to expired token that could not be refreshed: {}", identifiant);
                }
            }
        }

        filterChain.doFilter(request, response);
    }
    
    
    
}