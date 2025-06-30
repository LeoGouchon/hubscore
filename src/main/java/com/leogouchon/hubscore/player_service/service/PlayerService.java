package com.leogouchon.hubscore.player_service.service;

import com.leogouchon.hubscore.player_service.entity.Players;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PlayerService {
    Page<Players> getPlayers(int page, int size);
    Optional<Players> getPlayer(UUID id);
    Players createPlayer(Players player);
    void deletePlayer(UUID id);
    List<Players> getUnassociatedPlayers();
}
