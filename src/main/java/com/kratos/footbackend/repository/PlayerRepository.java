package com.kratos.footbackend.repository;

import com.kratos.footbackend.model.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PlayerRepository extends JpaRepository<Player, Long> {
    Optional<Player> findByRegisterNumber(String registerNumber);
    boolean existsByRegisterNumber(String registerNumber);
}
