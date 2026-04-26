package com.kratos.footbackend.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.kratos.footbackend.dto.CreateAcademyDto;
import com.kratos.footbackend.model.Academy;
import com.kratos.footbackend.repository.AcademyRepository;

@Service
public class AcademyService {
    private static final Logger logger = LoggerFactory.getLogger(AcademyService.class);

    private final AcademyRepository academyRepository;

    @Autowired
    public AcademyService(AcademyRepository academyRepository) {
        this.academyRepository = academyRepository;
    }

    public Academy createAcademy(CreateAcademyDto dto) {
        if (dto.getAcademyName() == null || dto.getAcademyName().isBlank()) {
            throw new RuntimeException("Le nom de l'académie est obligatoire");
        }
        if (academyRepository.existsByAcademyName(dto.getAcademyName())) {
            throw new RuntimeException("Une académie avec ce nom existe déjà");
        }

        Academy academy = new Academy();
        academy.setAcademyName(dto.getAcademyName());
        academy.setLocalite(dto.getLocalite());
        academy.setNumeroTelephone(dto.getNumeroTelephone());
        academy.setDescription(dto.getDescription());
        academy.setCreatedAt(LocalDateTime.now());

        Academy saved = academyRepository.save(academy);
        logger.info("Academy created: {}", saved.getAcademyName());
        return saved;
    }

    public List<Academy> getAllAcademies() {
        return academyRepository.findAll();
    }

    public Optional<Academy> getAcademyById(Long id) {
        return academyRepository.findById(id);
    }

    public Academy updateAcademy(Long id, CreateAcademyDto dto) {
        Academy academy = academyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Académie non trouvée"));

        if (dto.getAcademyName() != null &&
                !dto.getAcademyName().equals(academy.getAcademyName()) &&
                academyRepository.existsByAcademyName(dto.getAcademyName())) {
            throw new RuntimeException("Une académie avec ce nom existe déjà");
        }

        if (dto.getAcademyName() != null) academy.setAcademyName(dto.getAcademyName());
        if (dto.getLocalite() != null) academy.setLocalite(dto.getLocalite());
        if (dto.getNumeroTelephone() != null) academy.setNumeroTelephone(dto.getNumeroTelephone());
        if (dto.getDescription() != null) academy.setDescription(dto.getDescription());

        Academy updated = academyRepository.save(academy);
        logger.info("Academy updated: {}", updated.getAcademyName());
        return updated;
    }

    public void deleteAcademy(Long id) {
        if (!academyRepository.existsById(id)) {
            throw new RuntimeException("Académie non trouvée");
        }
        academyRepository.deleteById(id);
        logger.info("Academy deleted: id={}", id);
    }
}
