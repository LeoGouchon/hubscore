package com.leogouchon.hubscore.squash_match_service.controller;

import com.leogouchon.hubscore.squash_match_service.dto.OverallStatsResponseDTO;
import com.leogouchon.hubscore.squash_match_service.dto.PlayerStatsResponseDTO;
import com.leogouchon.hubscore.squash_match_service.service.SquashMatchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping(value = "/api/v1/squash/stats")
@Tag(name = "Squash - Stats")
public class SquashStatControllerV1 {

    private final SquashMatchService matchService;

    @Autowired
    public SquashStatControllerV1(SquashMatchService matchService) {
        this.matchService = matchService;
    }


    @Deprecated
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Return global data",
            description = "Return overall stats from the database squash matches"
    )
    @ApiResponse(responseCode = "200", description = "Matches dates found")
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = {@Content(schema = @Schema())})
    @GetMapping("/global")
    public ResponseEntity<OverallStatsResponseDTO> getOverallStats() {
        OverallStatsResponseDTO response = matchService.getOverallStats();
        return ResponseEntity.ok(response);
    }

    @Deprecated
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Return overall data from a specific player"
    )
    @ApiResponse(responseCode = "200", description = "Player data found")
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = {@Content(schema = @Schema())})
    @GetMapping("/player/{id:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}}")
    public ResponseEntity<PlayerStatsResponseDTO> getPlayerOverallStats(@PathVariable UUID id) {
        PlayerStatsResponseDTO response = matchService.getPlayerStats(id);
        return ResponseEntity.ok(response);
    }
}
