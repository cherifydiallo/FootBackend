package com.kratos.footbackend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.kratos.footbackend.model.Player;

@Repository
public interface PlayerRepository extends JpaRepository<Player, Long>, JpaSpecificationExecutor<Player> {
    Optional<Player> findByRegisterNumber(String registerNumber);
    boolean existsByRegisterNumber(String registerNumber);
}
