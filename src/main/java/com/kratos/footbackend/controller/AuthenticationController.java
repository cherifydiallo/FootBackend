package com.kratos.footbackend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kratos.footbackend.dto.LoginUserDto;
import com.kratos.footbackend.dto.RegisterUserDto;
import com.kratos.footbackend.model.User;
import com.kratos.footbackend.service.AuthenticationService;
import com.kratos.footbackend.service.JwtService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@RequestMapping("/auth")
@RestController
public class AuthenticationController {
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationController.class);

    private final JwtService jwtService;
    private final AuthenticationService authenticationService;

    public AuthenticationController(JwtService jwtService, AuthenticationService authenticationService) {
        this.jwtService = jwtService;
        this.authenticationService = authenticationService;
    }

   
    
    
    @PostMapping("/signup")
    public ResponseEntity<Map<String, Object>> register(@RequestBody RegisterUserDto registerUserDto) {
        try {
            User registeredUser = authenticationService.signup(registerUserDto);
            logger.info("User registered successfully: {}", registeredUser.getIdentifiant());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Utilisateur enregistré avec succès");
            response.put("user", registeredUser);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            logger.warn("Signup failed: {}", e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            logger.error("Signup error: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Erreur serveur");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> authenticate(@RequestBody LoginUserDto loginUserDto) {
        try {
            User authenticatedUser = authenticationService.authenticate(loginUserDto);

            if (authenticatedUser != null) {
                String jwtToken = jwtService.generateToken(authenticatedUser.getIdentifiant());
                long expirationTime = jwtService.getExpirationTime();

                logger.info("User logged in successfully: {}", authenticatedUser.getIdentifiant());

                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Connexion réussie");
                response.put("token", jwtToken);
                response.put("expiresIn", expirationTime);
                return ResponseEntity.ok(response);
            } else {
                logger.warn("Login failed for user: {}", loginUserDto.getIdentifiant());
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Identifiant ou mot de passe incorrect");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
        } catch (Exception e) {
            logger.error("Login error: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Erreur serveur");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/check")
    public ResponseEntity<Map<String, Object>> checkToken(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Token is valid");
        response.put("user", authentication.getName());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<Map<String, Object>> refreshToken(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        if (refreshToken == null || refreshToken.isEmpty()) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Refresh token is missing");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            if (!jwtService.validateJwtToken(refreshToken)) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Invalid refresh token");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            String identifiant = jwtService.getIdentifiantFromToken(refreshToken);
            User user = authenticationService.loadUserByIdentifiant(identifiant);
            if (user == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "User not found");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            String newAccessToken = jwtService.generateToken(identifiant);
            logger.info("Token refreshed for user: {}", identifiant);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("token", newAccessToken);
            response.put("expiresIn", jwtService.getExpirationTime());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error processing refresh token: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error processing refresh token");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/user/search")
    public ResponseEntity<Map<String, Object>> searchByUsername(@RequestParam String username) {
        if (username == null || username.trim().isEmpty()) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Username parameter is required");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        List<User> users = authenticationService.searchUsersByUsername(username);
        
        List<Map<String, Object>> usersList = new ArrayList<>();
        for (User user : users) {
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", user.getId());
            userInfo.put("identifiant", user.getIdentifiant());
            userInfo.put("fullName", user.getFullName());
            userInfo.put("email", user.getEmail());
            userInfo.put("role", user.getRole() != null ? user.getRole().name() : null);
            userInfo.put("status", user.getStatus());
            userInfo.put("joinedDate", user.getJoinedDate());
            usersList.add(userInfo);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Search results for username: " + username);
        response.put("count", usersList.size());
        response.put("users", usersList);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<Map<String, Object>> updateUser(@PathVariable Long id, @RequestBody RegisterUserDto registerUserDto) {
        try {
            User updatedUser = authenticationService.updateUser(id, registerUserDto);
            logger.info("User updated: {}", updatedUser.getIdentifiant());

            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", updatedUser.getId());
            userInfo.put("identifiant", updatedUser.getIdentifiant());
            userInfo.put("fullName", updatedUser.getFullName());
            userInfo.put("email", updatedUser.getEmail());
            userInfo.put("role", updatedUser.getRole() != null ? updatedUser.getRole().name() : null);
            userInfo.put("status", updatedUser.getStatus());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Utilisateur mis à jour avec succès");
            response.put("user", userInfo);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            logger.warn("User update failed: {}", e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            logger.error("User update error: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Erreur serveur");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable Long id) {
        try {
            authenticationService.deleteUser(id);
            logger.info("User deleted with id: {}", id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Utilisateur supprimé avec succès");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            logger.warn("User delete failed: {}", e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            logger.error("User delete error: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Erreur serveur");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/users")
    public ResponseEntity<Map<String, Object>> getAllUsers() {
        try {
            List<User> users = authenticationService.getAllUsers();
            
            List<Map<String, Object>> usersList = new ArrayList<>();
            for (User user : users) {
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("id", user.getId());
                userInfo.put("identifiant", user.getIdentifiant());
                userInfo.put("fullName", user.getFullName());
                userInfo.put("email", user.getEmail());
                userInfo.put("role", user.getRole() != null ? user.getRole().name() : null);
                userInfo.put("status", user.getStatus());
                userInfo.put("joinedDate", user.getJoinedDate());
                userInfo.put("lastActive", user.getLastActive());
                usersList.add(userInfo);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("count", usersList.size());
            response.put("users", usersList);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error fetching users: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Erreur serveur");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}