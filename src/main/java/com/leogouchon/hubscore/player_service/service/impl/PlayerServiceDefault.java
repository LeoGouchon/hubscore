package com.leogouchon.hubscore.player_service.service.impl;

import com.leogouchon.hubscore.player_service.dto.PlayerRequestDTO;
import com.leogouchon.hubscore.player_service.entity.PlayerTeam;
import com.leogouchon.hubscore.player_service.entity.Players;
import com.leogouchon.hubscore.player_service.entity.Teams;
import com.leogouchon.hubscore.player_service.repository.PlayerRepository;
import com.leogouchon.hubscore.player_service.repository.PlayerTeamRepository;
import com.leogouchon.hubscore.player_service.repository.TeamRepository;
import com.leogouchon.hubscore.player_service.service.PlayerService;
import com.leogouchon.hubscore.player_service.service.TeamService;
import com.leogouchon.hubscore.player_service.specification.PlayerSpecifications;
import jakarta.transaction.Transactional;
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
    private final TeamRepository teamRepository;
    private final PlayerTeamRepository playerTeamRepository;

    @Autowired
    public PlayerServiceDefault(
            PlayerRepository playerRepository,
            TeamRepository teamRepository,
            PlayerTeamRepository playerTeamRepository
    ) {
        this.playerRepository = playerRepository;
        this.teamRepository = teamRepository;
        this.playerTeamRepository = playerTeamRepository;
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

    @Transactional
    @Override
    public Players createPlayer(PlayerRequestDTO player) throws RuntimeException {
        if (player.getFirstname() == null || player.getLastname() == null) {
            throw new RuntimeException("Firstname and lastname must not be null");
        }
        try {
            Players savedPlayer = playerRepository.save(new Players(player.getFirstname(), player.getLastname()));
            player.getTeamIds().forEach(teamId -> {
                Optional<Teams> teamsOpt = teamRepository.findById(teamId);
                teamsOpt.ifPresent(team -> {
                    PlayerTeam playerTeamNew = new PlayerTeam(savedPlayer, team);
                    playerTeamRepository.save(playerTeamNew);
                });
            });
            return savedPlayer;
        } catch (RuntimeException e) {
            throw new RuntimeException("Failed to create player", e);
        }
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
