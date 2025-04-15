package com.leogouchon.squashapp.controller;

import com.leogouchon.squashapp.model.Players;
import com.leogouchon.squashapp.service.interfaces.IPlayerService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/players")
@Tag(name = "Player")
public class PlayerController {

    private final IPlayerService playerService;

    @Autowired
    public PlayerController(IPlayerService playerService) {
        this.playerService = playerService;
    }

    @GetMapping
    public ResponseEntity<List<Players>> getPlayers() {
        Optional<List<Players>> players = playerService.getPlayers();
        return players.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
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
        Optional<List<Players>> unassociatedPlayers = playerService.getUnassociatedPlayers();
        return unassociatedPlayers.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }
}