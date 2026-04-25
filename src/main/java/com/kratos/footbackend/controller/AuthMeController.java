package com.kratos.footbackend.controller;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kratos.footbackend.model.User;
import com.kratos.footbackend.service.AuthenticationService;

@RestController
@RequestMapping("/profile")
public class AuthMeController {
    private static final Logger logger = LoggerFactory.getLogger(AuthMeController.class);

    private final AuthenticationService authenticationService;

    public AuthMeController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @GetMapping("/me")
    @PreAuthorize("@restPermissionEvaluator.canExecute(authentication, 'profile_view')")
    public ResponseEntity<Map<String, Object>> me(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Unauthorized");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        String identifiant = authentication.getName();
        if (authentication.getPrincipal() instanceof UserDetails userDetails) {
            identifiant = userDetails.getUsername();
        }

        User user = authenticationService.loadUserByIdentifiant(identifiant);
        if (user == null) {
            logger.warn("Authenticated user not found: {}", identifiant);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "User not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("identifiant", user.getIdentifiant());
        userInfo.put("fullName", user.getFullName());
        userInfo.put("email", user.getEmail());
        userInfo.put("role", user.getRole() != null ? user.getRole().name() : null);
        userInfo.put("status", user.getStatus());
        userInfo.put("joinedDate", user.getJoinedDate());
        userInfo.put("lastActive", user.getLastActive());

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "me");
        response.put("user", userInfo);
        return ResponseEntity.ok(response);
    }
}
