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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kratos.footbackend.dto.CreateAcademyCategoryDto;
import com.kratos.footbackend.model.AcademyCategory;
import com.kratos.footbackend.service.AcademyCategoryService;

@RestController
@RequestMapping("/academies")
public class AcademyCategoryController {
    private static final Logger logger = LoggerFactory.getLogger(AcademyCategoryController.class);

    private final AcademyCategoryService categoryService;

    @Autowired
    public AcademyCategoryController(AcademyCategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping("/{academyId}/categories")
    @PreAuthorize("@restPermissionEvaluator.canExecute(authentication, 'academy_edit')")
    public ResponseEntity<Map<String, Object>> createCategory(
            @PathVariable Long academyId,
            @RequestBody CreateAcademyCategoryDto dto) {
        dto.setAcademyId(academyId);
        try {
            AcademyCategory category = categoryService.createCategory(dto);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Catégorie créée avec succès");
            response.put("category", buildCategoryInfo(category));
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            logger.warn("Category creation failed: {}", e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            logger.error("Category creation error: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Erreur serveur");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/{academyId}/categories")
    @PreAuthorize("@restPermissionEvaluator.canExecute(authentication, 'academy_read')")
    public ResponseEntity<Map<String, Object>> getCategoriesByAcademy(@PathVariable Long academyId) {
        try {
            List<AcademyCategory> categories = categoryService.getCategoriesByAcademy(academyId);
            List<Map<String, Object>> list = new ArrayList<>();
            for (AcademyCategory c : categories) {
                list.add(buildCategoryInfo(c));
            }
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("count", list.size());
            response.put("categories", list);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            logger.warn("Get categories failed: {}", e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            logger.error("Get categories error: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Erreur serveur");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @DeleteMapping("/categories/{id}")
    @PreAuthorize("@restPermissionEvaluator.canExecute(authentication, 'academy_edit')")
    public ResponseEntity<Map<String, Object>> deleteCategory(@PathVariable Long id) {
        try {
            categoryService.deleteCategory(id);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Catégorie supprimée avec succès");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            logger.warn("Category delete failed: {}", e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            logger.error("Category delete error: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Erreur serveur");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    private Map<String, Object> buildCategoryInfo(AcademyCategory c) {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("id", c.getId());
        info.put("name", c.getName());
        info.put("academyId", c.getAcademy().getId());
        info.put("academyName", c.getAcademy().getAcademyName());
        return info;
    }
}
