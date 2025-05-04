package com.leogouchon.squashapp.controller;

import com.leogouchon.squashapp.model.Players;
import com.leogouchon.squashapp.service.interfaces.IPlayerService;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(
        value = "/api/players")
@Tag(name = "Player")
public class PlayerController {

    private final IPlayerService playerService;

    @Autowired
    public PlayerController(IPlayerService playerService) {
        this.playerService = playerService;
    }

    // TODO : add offset and limit
    @GetMapping
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponse(responseCode = "200", description = "Players found")
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = {@Content(schema = @Schema())})
    public ResponseEntity<List<Players>> getPlayers() {
        List<Players> players = playerService.getPlayers();
        return ResponseEntity.ok(players);
    }

    @GetMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponse(responseCode = "200", description = "Player with given id found")
    @ApiResponse(responseCode = "404", description = "Player not found", content = {@Content(schema = @Schema())})
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = {@Content(schema = @Schema())})
    public ResponseEntity<Players> getPlayer(@PathVariable Long id) {
        Optional<Players> player = playerService.getPlayer(id);
        return player.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    @ApiResponse(responseCode = "201", description = "Player created")
    @ApiResponse(responseCode = "400", description = "Bad request", content = {@Content(schema = @Schema())})
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = {@Content(schema = @Schema())})
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Players> createPlayer(@RequestBody Players player) {
        Players createdPlayer;
        try {
            createdPlayer = playerService.createPlayer(player);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
        URI location = URI.create("/api/players/" + createdPlayer.getId());
        return ResponseEntity.created(location).body(createdPlayer);
    }

    @DeleteMapping("/{id}")
    @ApiResponse(responseCode = "204", description = "Player deleted successfully")
    @ApiResponse(responseCode = "404", description = "Player to delete not found", content = {@Content(schema = @Schema())})
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = {@Content(schema = @Schema())})
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> deletePlayer(@PathVariable Long id) {
        try {
            playerService.deletePlayer(id);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/unlinked")
    @ApiResponse(responseCode = "200", description = "Players without linked user found")
    public ResponseEntity<List<Players>> getUnassociatedPlayers() {
        List<Players> unassociatedPlayers = playerService.getUnassociatedPlayers();
        return ResponseEntity
                .ok()
                .body(unassociatedPlayers);
    }
}