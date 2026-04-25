package com.kratos.footbackend.controller;

import com.kratos.footbackend.dto.CreatePlayerDto;
import com.kratos.footbackend.model.Player;
import com.kratos.footbackend.service.PlayerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

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

    @PutMapping("/{id}")
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
        info.put("academy", p.getAcademy());
        info.put("category", p.getCategory());
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
