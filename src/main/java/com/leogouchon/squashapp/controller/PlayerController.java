package com.leogouchon.squashapp.controller;

import com.leogouchon.squashapp.model.Players;
import com.leogouchon.squashapp.service.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/players")
public class PlayerController {

    private final PlayerService playerService;

    @Autowired
    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }

    @GetMapping
    public ResponseEntity<List<Players>> getPlayers() {
        List<Players> players = playerService.getPlayers();
        return ResponseEntity.ok(players);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Players> getPlayer(@PathVariable Long id) {
        Optional<Players> player = playerService.getPlayer(id);
        return player.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }
    
    @PostMapping
    public ResponseEntity<Players> createPlayer(@RequestBody Players player) {
        Players createdPlayer = playerService.createPlayer(player);
        return ResponseEntity.ok(createdPlayer);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePlayer(@PathVariable Long id) {
        playerService.deletePlayer(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/unlinked")
    public ResponseEntity<List<Players>> getUnassociatedPlayers() {
        List<Players> unassociatedPlayers = playerService.getUnassociatedPlayers();
        return ResponseEntity.ok(unassociatedPlayers);
    }
}