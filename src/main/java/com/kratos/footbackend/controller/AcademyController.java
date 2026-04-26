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
import org.springframework.web.bind.annotation.RestController;

import com.kratos.footbackend.dto.CreateAcademyDto;
import com.kratos.footbackend.model.Academy;
import com.kratos.footbackend.service.AcademyService;

@RequestMapping("/academies")
@RestController
public class AcademyController {
    private static final Logger logger = LoggerFactory.getLogger(AcademyController.class);

    private final AcademyService academyService;

    @Autowired
    public AcademyController(AcademyService academyService) {
        this.academyService = academyService;
    }

    @PostMapping
    @PreAuthorize("@restPermissionEvaluator.canExecute(authentication, 'academy_write')")
    public ResponseEntity<Map<String, Object>> createAcademy(@RequestBody CreateAcademyDto dto) {
        try {
            Academy academy = academyService.createAcademy(dto);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Académie créée avec succès");
            response.put("academy", buildAcademyInfo(academy));
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            logger.warn("Academy creation failed: {}", e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            logger.error("Academy creation error: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Erreur serveur");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/all")
    @PreAuthorize("@restPermissionEvaluator.canExecute(authentication, 'academy_read')")
    public ResponseEntity<Map<String, Object>> getAllAcademies() {
        try {
            List<Academy> academies = academyService.getAllAcademies();
            List<Map<String, Object>> list = new ArrayList<>();
            for (Academy a : academies) {
                list.add(buildAcademyInfo(a));
            }
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("count", list.size());
            response.put("academies", list);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Get all academies error: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Erreur serveur");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("@restPermissionEvaluator.canExecute(authentication, 'academy_read')")
    public ResponseEntity<Map<String, Object>> getAcademyById(@PathVariable Long id) {
        try {
            return academyService.getAcademyById(id)
                    .map(a -> {
                        Map<String, Object> response = new HashMap<>();
                        response.put("success", true);
                        response.put("academy", buildAcademyInfo(a));
                        return ResponseEntity.ok(response);
                    })
                    .orElseGet(() -> {
                        Map<String, Object> response = new HashMap<>();
                        response.put("success", false);
                        response.put("message", "Académie non trouvée");
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
                    });
        } catch (Exception e) {
            logger.error("Get academy by id error: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Erreur serveur");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("@restPermissionEvaluator.canExecute(authentication, 'academy_edit')")
    public ResponseEntity<Map<String, Object>> updateAcademy(@PathVariable Long id, @RequestBody CreateAcademyDto dto) {
        try {
            Academy academy = academyService.updateAcademy(id, dto);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Académie mise à jour avec succès");
            response.put("academy", buildAcademyInfo(academy));
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            logger.warn("Academy update failed: {}", e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            logger.error("Academy update error: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Erreur serveur");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@restPermissionEvaluator.canExecute(authentication, 'academy_delete')")
    public ResponseEntity<Map<String, Object>> deleteAcademy(@PathVariable Long id) {
        try {
            academyService.deleteAcademy(id);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Académie supprimée avec succès");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            logger.warn("Academy delete failed: {}", e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            logger.error("Academy delete error: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Erreur serveur");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    private Map<String, Object> buildAcademyInfo(Academy a) {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("id", a.getId());
        info.put("academyName", a.getAcademyName());
        info.put("localite", a.getLocalite());
        info.put("numeroTelephone", a.getNumeroTelephone());
        info.put("description", a.getDescription());
        info.put("createdAt", a.getCreatedAt());
        return info;
    }
}
