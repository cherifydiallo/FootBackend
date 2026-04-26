package com.kratos.footbackend.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.kratos.footbackend.dto.CreateAcademyCategoryDto;
import com.kratos.footbackend.model.Academy;
import com.kratos.footbackend.model.AcademyCategory;
import com.kratos.footbackend.repository.AcademyCategoryRepository;
import com.kratos.footbackend.repository.AcademyRepository;

@Service
public class AcademyCategoryService {
    private static final Logger logger = LoggerFactory.getLogger(AcademyCategoryService.class);

    private final AcademyCategoryRepository categoryRepository;
    private final AcademyRepository academyRepository;

    @Autowired
    public AcademyCategoryService(AcademyCategoryRepository categoryRepository, AcademyRepository academyRepository) {
        this.categoryRepository = categoryRepository;
        this.academyRepository = academyRepository;
    }

    public AcademyCategory createCategory(CreateAcademyCategoryDto dto) {
        if (dto.getName() == null || dto.getName().isBlank()) {
            throw new RuntimeException("Le nom de la catégorie est obligatoire");
        }
        Academy academy = academyRepository.findById(dto.getAcademyId())
                .orElseThrow(() -> new RuntimeException("Académie non trouvée"));

        if (categoryRepository.existsByAcademyIdAndName(dto.getAcademyId(), dto.getName())) {
            throw new RuntimeException("Cette catégorie existe déjà pour cette académie");
        }

        AcademyCategory category = new AcademyCategory();
        category.setAcademy(academy);
        category.setName(dto.getName());

        AcademyCategory saved = categoryRepository.save(category);
        logger.info("Category '{}' created for academy '{}'", saved.getName(), academy.getAcademyName());
        return saved;
    }

    public List<AcademyCategory> getCategoriesByAcademy(Long academyId) {
        if (!academyRepository.existsById(academyId)) {
            throw new RuntimeException("Académie non trouvée");
        }
        return categoryRepository.findByAcademyId(academyId);
    }

    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new RuntimeException("Catégorie non trouvée");
        }
        categoryRepository.deleteById(id);
        logger.info("Category deleted: id={}", id);
    }
}
