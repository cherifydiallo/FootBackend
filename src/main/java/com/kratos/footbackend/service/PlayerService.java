package com.kratos.footbackend.service;

import com.kratos.footbackend.dto.CreatePlayerDto;
import com.kratos.footbackend.model.Player;
import com.kratos.footbackend.repository.PlayerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PlayerService {
    private static final Logger logger = LoggerFactory.getLogger(PlayerService.class);

    private final PlayerRepository playerRepository;

    @Autowired
    public PlayerService(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    public Player createPlayer(CreatePlayerDto dto) {
        if (dto.getRegisterNumber() != null && playerRepository.existsByRegisterNumber(dto.getRegisterNumber())) {
            throw new RuntimeException("Un joueur avec ce numéro d'inscription existe déjà");
        }

        Player player = new Player();
        player.setFullName(dto.getFullName());
        player.setBirthDate(dto.getBirthDate());
        player.setAcademy(dto.getAcademy());
        player.setCategory(dto.getCategory());
        player.setRegisterNumber(dto.getRegisterNumber());
        player.setHeightCm(dto.getHeightCm());
        player.setWeightKg(dto.getWeightKg());
        player.setFatherName(dto.getFatherName());
        player.setMotherName(dto.getMotherName());
        player.setPhoto(dto.getPhoto());
        player.setCreatedAt(LocalDateTime.now());

        Player saved = playerRepository.save(player);
        logger.info("Player created: {}", saved.getFullName());
        return saved;
    }

    public List<Player> getAllPlayers() {
        return playerRepository.findAll();
    }

    public Optional<Player> getPlayerById(Long id) {
        return playerRepository.findById(id);
    }

    public Player updatePlayer(Long id, CreatePlayerDto dto) {
        Player player = playerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Joueur non trouvé"));

        if (dto.getRegisterNumber() != null &&
                !dto.getRegisterNumber().equals(player.getRegisterNumber()) &&
                playerRepository.existsByRegisterNumber(dto.getRegisterNumber())) {
            throw new RuntimeException("Un joueur avec ce numéro d'inscription existe déjà");
        }

        player.setFullName(dto.getFullName());
        player.setBirthDate(dto.getBirthDate());
        player.setAcademy(dto.getAcademy());
        player.setCategory(dto.getCategory());
        player.setRegisterNumber(dto.getRegisterNumber());
        player.setHeightCm(dto.getHeightCm());
        player.setWeightKg(dto.getWeightKg());
        player.setFatherName(dto.getFatherName());
        player.setMotherName(dto.getMotherName());
        player.setPhoto(dto.getPhoto());

        Player updated = playerRepository.save(player);
        logger.info("Player updated: {}", updated.getFullName());
        return updated;
    }

    public Optional<Player> getPlayerByRegisterNumber(String registerNumber) {
        return playerRepository.findByRegisterNumber(registerNumber);
    }

    public void deletePlayer(Long id) {
        Player player = playerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Joueur non trouvé"));
        playerRepository.delete(player);
        logger.info("Player deleted: {}", player.getFullName());
    }
}
