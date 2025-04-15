package com.leogouchon.squashapp.service.interfaces;

import com.leogouchon.squashapp.model.Players;

import java.util.List;
import java.util.Optional;

public interface IPlayerService {
    List<Players> getPlayers();
    Optional<Players> getPlayer(Long id);
    Players createPlayer(Players player);
    void deletePlayer(Long id);
    List<Players> getUnassociatedPlayers();
}
