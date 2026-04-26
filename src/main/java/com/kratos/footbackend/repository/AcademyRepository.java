package com.kratos.footbackend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kratos.footbackend.model.Academy;

@Repository
public interface AcademyRepository extends JpaRepository<Academy, Long> {
    Optional<Academy> findByAcademyName(String academyName);
    boolean existsByAcademyName(String academyName);
}
