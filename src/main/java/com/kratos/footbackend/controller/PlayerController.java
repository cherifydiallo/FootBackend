package com.kratos.footbackend.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kratos.footbackend.dto.CreatePlayerDto;
import com.kratos.footbackend.model.Player;
import com.kratos.footbackend.service.PlayerService;

@RequestMapping("/players")
@RestController
public class PlayerController {
    private static final Logger logger = LoggerFactory.getLogger(PlayerController.class);

    private final PlayerService playerService;

    @Autowired
    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }

    @PostMapping
    @PreAuthorize("@restPermissionEvaluator.canExecute(authentication, 'player_edit')")
    public ResponseEntity<Map<String, Object>> createPlayer(@RequestBody CreatePlayerDto dto) {
        try {
            Player player = playerService.createPlayer(dto);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Joueur créé avec succès");
            response.put("player", buildPlayerInfo(player));
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            logger.warn("Player creation failed: {}", e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            logger.error("Player creation error: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Erreur serveur");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/all")
    @PreAuthorize("@restPermissionEvaluator.canExecute(authentication, 'player_read')")
    public ResponseEntity<Map<String, Object>> getAllPlayers() {
        try {
            List<Player> players = playerService.getAllPlayers();
            List<Map<String, Object>> list = new ArrayList<>();
            for (Player p : players) {
                list.add(buildPlayerInfo(p));
            }
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("count", list.size());
            response.put("players", list);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error fetching players: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Erreur serveur");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("@restPermissionEvaluator.canExecute(authentication, 'player_read')")
    public ResponseEntity<Map<String, Object>> getPlayerById(@PathVariable Long id) {
        try {
            return playerService.getPlayerById(id)
                    .map(p -> {
                        Map<String, Object> response = new HashMap<>();
                        response.put("success", true);
                        response.put("player", buildPlayerInfo(p));
                        return ResponseEntity.ok(response);
                    })
                    .orElseGet(() -> {
                        Map<String, Object> response = new HashMap<>();
                        response.put("success", false);
                        response.put("message", "Joueur non trouvé");
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
                    });
        } catch (Exception e) {
            logger.error("Error fetching player: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Erreur serveur");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/search")
    @PreAuthorize("@restPermissionEvaluator.canExecute(authentication, 'player_read')")
    public ResponseEntity<Map<String, Object>> searchByRegisterNumber(@RequestParam String registerNumber) {
        if (registerNumber == null || registerNumber.trim().isEmpty()) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Le numéro d'inscription est requis");
            return ResponseEntity.badRequest().body(response);
        }
        return playerService.getPlayerByRegisterNumber(registerNumber.trim())
                .map(p -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", true);
                    response.put("player", buildPlayerInfo(p));
                    return ResponseEntity.ok(response);
                })
                .orElseGet(() -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", false);
                    response.put("message", "Aucun joueur trouvé avec ce numéro d'inscription");
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
                });
    }

    @GetMapping("/search-advanced")
    @PreAuthorize("@restPermissionEvaluator.canExecute(authentication, 'player_read')")
    public ResponseEntity<Map<String, Object>> searchAdvanced(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String birthDate,
            @RequestParam(required = false) Integer heightCm,
            @RequestParam(required = false) Integer weightKg,
            @RequestParam(required = false) String createdAt
    ) {
        try {
            if (name != null && name.trim().length() > 0 && name.trim().length() < 3) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Le nom doit contenir au moins 3 caractères pour la recherche");
                return ResponseEntity.badRequest().body(response);
            }

            java.time.LocalDate birthDateParsed = null;
            java.time.LocalDate createdAtParsed = null;
            try {
                if (birthDate != null && !birthDate.trim().isEmpty()) {
                    birthDateParsed = java.time.LocalDate.parse(birthDate.trim());
                }
                if (createdAt != null && !createdAt.trim().isEmpty()) {
                    createdAtParsed = java.time.LocalDate.parse(createdAt.trim());
                }
            } catch (Exception e) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Format de date invalide. Utilisez ISO yyyy-MM-dd");
                return ResponseEntity.badRequest().body(response);
            }

            List<Player> players = playerService.searchPlayers(name, categoryId, birthDateParsed, heightCm, weightKg, createdAtParsed);
            List<Map<String, Object>> list = new ArrayList<>();
            for (Player p : players) {
                list.add(buildPlayerInfo(p));
            }
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("count", list.size());
            response.put("players", list);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Advanced search error: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Erreur serveur");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("@restPermissionEvaluator.canExecute(authentication, 'player_edit')")
    public ResponseEntity<Map<String, Object>> updatePlayer(@PathVariable Long id, @RequestBody CreatePlayerDto dto) {
        try {
            Player player = playerService.updatePlayer(id, dto);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Joueur mis à jour avec succès");
            response.put("player", buildPlayerInfo(player));
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            logger.warn("Player update failed: {}", e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            logger.error("Player update error: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Erreur serveur");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@restPermissionEvaluator.canExecute(authentication, 'player_delete')")
    public ResponseEntity<Map<String, Object>> deletePlayer(@PathVariable Long id) {
        try {
            playerService.deletePlayer(id);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Joueur supprimé avec succès");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            logger.warn("Player delete failed: {}", e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            logger.error("Player delete error: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Erreur serveur");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    private Map<String, Object> buildPlayerInfo(Player p) {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("id", p.getId());
        info.put("fullName", p.getFullName());
        info.put("birthDate", p.getBirthDate());
        if (p.getAcademy() != null) {
            Map<String, Object> academyInfo = new LinkedHashMap<>();
            academyInfo.put("id", p.getAcademy().getId());
            academyInfo.put("academyName", p.getAcademy().getAcademyName());
            info.put("academy", academyInfo);
        } else {
            info.put("academy", null);
        }
        info.put("category", p.getCategory() != null
                ? Map.of("id", p.getCategory().getId(), "name", p.getCategory().getName())
                : null);
        info.put("registerNumber", p.getRegisterNumber());
        info.put("heightCm", p.getHeightCm());
        info.put("weightKg", p.getWeightKg());
        info.put("fatherName", p.getFatherName());
        info.put("motherName", p.getMotherName());
        info.put("photo", p.getPhoto());
        info.put("createdAt", p.getCreatedAt());
        return info;
    }
}
