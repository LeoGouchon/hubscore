package com.leogouchon.squashapp.service;

import com.leogouchon.squashapp.model.Players;
import com.leogouchon.squashapp.model.Users;
import com.leogouchon.squashapp.repository.PlayerRepository;
import com.leogouchon.squashapp.service.interfaces.IPlayerService;
import com.leogouchon.squashapp.service.interfaces.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PlayerService implements IPlayerService {

    private final PlayerRepository playerRepository;
    private final IUserService userService;

    @Autowired
    public PlayerService(
            PlayerRepository playerRepository,
            IUserService userService
    ) {
        this.playerRepository = playerRepository;
        this.userService = userService;
    }

    public List<Players> getPlayers() {
        return playerRepository.findAll();
    }

    public Optional<Players> getPlayer(Long id) {
        return playerRepository.findById(id);
    }

    public Players createPlayer(Players player) {
        if (player.getFirstname() == null || player.getLastname() == null) {
            throw new RuntimeException("Firstname and lastname must not be null");
        }
        return playerRepository.save(player);
    }

    public void deletePlayer(Long id) {
        if (playerRepository.existsById(id)) {
            playerRepository.deleteById(id);
        } else {
            throw new RuntimeException("Player not found with id: " + id);
        }
    }

    public List<Players> getUnassociatedPlayers() {
        List<Players> players = getPlayers();
        List<Users> users = userService.getUsersWithLinkedPlayer();
        List<Long> alreadyLinkedPlayerIds = users.stream().map(u -> u.getPlayer().getId()).toList();
        players.removeIf(p -> alreadyLinkedPlayerIds.contains(p.getId()));
        return players;
    }
}
