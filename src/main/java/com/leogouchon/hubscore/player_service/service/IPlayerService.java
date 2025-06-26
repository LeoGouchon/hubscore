package com.leogouchon.hubscore.player_service.service;

import com.leogouchon.hubscore.player_service.entity.Players;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;

public interface IPlayerService {
    Page<Players> getPlayers(int page, int size);
    Optional<Players> getPlayer(Long id);
    Players createPlayer(Players player);
    void deletePlayer(Long id);
    List<Players> getUnassociatedPlayers();
}
