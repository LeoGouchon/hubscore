package com.leogouchon.hubscore.player_service.service.impl;

import com.leogouchon.hubscore.player_service.entity.Players;
import com.leogouchon.hubscore.player_service.repository.PlayerRepository;
import com.leogouchon.hubscore.player_service.service.PlayerService;
import com.leogouchon.hubscore.player_service.specification.PlayerSpecifications;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class PlayerServiceDefault implements PlayerService {

    private final PlayerRepository playerRepository;

    @Autowired
    public PlayerServiceDefault(
            PlayerRepository playerRepository
    ) {
        this.playerRepository = playerRepository;
    }

    public Page<Players> getPlayers(int page, int size, String sport, String teamId) {
        Specification<Players> spec = (root, query, cb) -> cb.conjunction();

        if (sport != null && !sport.isBlank()) {
            spec = spec.and(PlayerSpecifications.bySport(sport));
        }

        if (isValidUuid(teamId)) {
            spec = spec.and(PlayerSpecifications.byTeam(UUID.fromString(teamId)));
        }

        Pageable pageable = PageRequest.of(page, size);
        return playerRepository.findAll(spec, pageable);
    }

    public Optional<Players> getPlayer(UUID id) {
        if (id == null) return Optional.empty();
        return playerRepository.findById(id);
    }

    public Players createPlayer(Players player) throws RuntimeException {
        if (player.getFirstname() == null || player.getLastname() == null) {
            throw new RuntimeException("Firstname and lastname must not be null");
        }
        return playerRepository.save(player);
    }

    public void deletePlayer(UUID id) throws RuntimeException {
        if (playerRepository.existsById(id)) {
            playerRepository.deleteById(id);
        } else {
            throw new RuntimeException("Player not found with id: " + id);
        }
    }

    public List<Players> getUnassociatedPlayers(String sport, String teamId) {
        Specification<Players> spec = (root, query, cb) -> cb.conjunction();
        
        if (sport != null && !sport.isBlank()) {
            spec = spec.and(PlayerSpecifications.bySport(sport));
        }
        
        if (isValidUuid(teamId)) {
            spec = spec.and(PlayerSpecifications.byTeam(UUID.fromString(teamId)));
        }
        
        spec = spec.and(PlayerSpecifications.withoutUser());

        return playerRepository.findAll(spec);
    }

    private boolean isValidUuid(String id) {
        return id != null && !id.isBlank()
                && !"undefined".equalsIgnoreCase(id)
                && !"null".equalsIgnoreCase(id);
    }
}
