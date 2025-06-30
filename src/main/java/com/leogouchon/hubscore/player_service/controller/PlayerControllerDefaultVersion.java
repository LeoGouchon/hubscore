package com.leogouchon.hubscore.player_service.controller;

import com.leogouchon.hubscore.common.dto.PaginatedResponseDTO;
import com.leogouchon.hubscore.player_service.entity.Players;
import com.leogouchon.hubscore.player_service.service.PlayerService;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@Deprecated(since="1.0.0", forRemoval=true)
@RequestMapping(
        value = "/api/players")
@Tag(name = "Player")
@Validated
public class PlayerControllerDefaultVersion {

    private final PlayerService playerService;

    @Autowired
    public PlayerControllerDefaultVersion(PlayerService playerService) {
        this.playerService = playerService;
    }

    @GetMapping
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponse(responseCode = "200", description = "Players found")
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = {@Content(schema = @Schema())})
    public ResponseEntity<PaginatedResponseDTO<Players>> getPlayers(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int size
    ) {
        Page<Players> playersPage = playerService.getPlayers(page, size);
        PaginatedResponseDTO<Players> response = new PaginatedResponseDTO<>(
                playersPage.getContent(),
                playersPage.getNumber(),
                playersPage.getTotalPages(),
                playersPage.getTotalElements(),
                playersPage.getSize()
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponse(responseCode = "200", description = "Player with given id found")
    @ApiResponse(responseCode = "404", description = "Player not found", content = {@Content(schema = @Schema())})
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = {@Content(schema = @Schema())})
    public ResponseEntity<Players> getPlayer(@PathVariable UUID id) {
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
    public ResponseEntity<Void> deletePlayer(@PathVariable UUID id) {
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