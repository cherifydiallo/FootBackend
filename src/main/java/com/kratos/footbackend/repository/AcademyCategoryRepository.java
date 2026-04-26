package com.kratos.footbackend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kratos.footbackend.model.AcademyCategory;

@Repository
public interface AcademyCategoryRepository extends JpaRepository<AcademyCategory, Long> {
    List<AcademyCategory> findByAcademyId(Long academyId);
    Optional<AcademyCategory> findByAcademyIdAndName(Long academyId, String name);
    boolean existsByAcademyIdAndName(Long academyId, String name);
}
