package com.kratos.footbackend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.kratos.footbackend.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByIdentifiant(String identifiant);
    boolean existsByIdentifiant(String identifiant);
    Optional<User> findById(Long id);
    
    @EntityGraph(attributePaths = "groups")
    @Query("SELECT u FROM User u WHERE u.identifiant = :identifiant")
    Optional<User> findOneByIdentifiantWithGroups(@Param("identifiant") String identifiant);
    
    @Query("SELECT u FROM User u WHERE LOWER(u.identifiant) LIKE LOWER(CONCAT(:username, '%'))")
    List<User> searchByUsername(@Param("username") String username);
}