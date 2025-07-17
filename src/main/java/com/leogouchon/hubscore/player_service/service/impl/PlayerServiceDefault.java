package com.leogouchon.hubscore.player_service.service.impl;

import com.leogouchon.hubscore.player_service.entity.Players;
import com.leogouchon.hubscore.player_service.repository.PlayerRepository;
import com.leogouchon.hubscore.player_service.service.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

    public Page<Players> getPlayers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return playerRepository.findAll(pageable);
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

    public List<Players> getUnassociatedPlayers() {
        return playerRepository.findPlayersWithoutUser();
    }
}
