package com.leogouchon.squashapp.service;

import com.leogouchon.squashapp.model.Player;
import com.leogouchon.squashapp.model.User;
import com.leogouchon.squashapp.repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PlayerService {

    private final PlayerRepository playerRepository;
    private final UserService userService;

    @Autowired
    public PlayerService(
            PlayerRepository playerRepository,
            UserService userService
    ) {
        this.playerRepository = playerRepository;
        this.userService = userService;
    }

    public List<Player> getPlayers() {
        return playerRepository.findAll();
    }

    public Optional<Player> getPlayer(Long id) {
        return playerRepository.findById(id);
    }

    public Player createPlayer(Player player) {
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

    public List<Player> getUnassociatedPlayers() {
        List<Player> players = getPlayers();
        List<User> users = userService.getUsersWithLinkedPlayer();
        List<Long> alreadyLinkedPlayerIds = users.stream().map(u -> u.getPlayer().getId()).toList();
        players.removeIf(p -> alreadyLinkedPlayerIds.contains(p.getId()));
        return players;
    }
}
