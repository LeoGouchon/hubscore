package com.leogouchon.hubscore.kicker_match_service.controller;

import com.leogouchon.hubscore.kicker_match_service.dto.MatrixScoreResultsResponseDTO;
import com.leogouchon.hubscore.kicker_match_service.dto.PlayerStatsResponseDTO;
import com.leogouchon.hubscore.kicker_match_service.dto.SeasonsStatsResponseDTO;
import com.leogouchon.hubscore.kicker_match_service.dto.GlobalStatsWithHistoryDTO;
import com.leogouchon.hubscore.kicker_match_service.service.EloMatrixService;
import com.leogouchon.hubscore.kicker_match_service.service.KickerStatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping(value = "/api/v1/kicker/stats")
@Tag(name = "Kicker")
@Tag(name = "Stats")
@Validated
public class KickerStatsController {

    private final KickerStatService kickerStatService;
    private final EloMatrixService eloMatrixService;

    public KickerStatsController(KickerStatService kickerStatService, EloMatrixService eloMatrixService) {
        this.kickerStatService = kickerStatService;
        this.eloMatrixService = eloMatrixService;
    }

    @Operation(
            summary = "Get global stats of all players",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Success",
                            content = @Content(schema = @Schema(implementation = GlobalStatsWithHistoryDTO.class))
                    )
            }
    )
    @GetMapping("/global")
    public ResponseEntity<List<GlobalStatsWithHistoryDTO>> getGlobalStats() {
        List<GlobalStatsWithHistoryDTO> globalStats = kickerStatService.getGlobalStats();

        return ResponseEntity.ok(globalStats);
    }

    @Operation(
            summary = "Get season stats of all players",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Success",
                            content = @Content(schema = @Schema(implementation = GlobalStatsWithHistoryDTO.class))
                    )
            }
    )
    @GetMapping("/season/{year}/{quarter}")
    public ResponseEntity<List<GlobalStatsWithHistoryDTO>> getSeasonStats(@PathVariable int year, @PathVariable int quarter) {
        List<GlobalStatsWithHistoryDTO> globalStats = kickerStatService.getSeasonStats(year, quarter);

        return ResponseEntity.ok(globalStats);
    }

    @Operation(
            summary = "Get quick seasons summary",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Success",
                            content = @Content(schema = @Schema(implementation = SeasonsStatsResponseDTO.class))
                    )
            }
    )
    @GetMapping("/season")
    public ResponseEntity<SeasonsStatsResponseDTO> getSeasonsStats() {
        SeasonsStatsResponseDTO seasonsStats = kickerStatService.getSeasonsStats();

        return ResponseEntity.ok(seasonsStats);
    }

    @Operation(
            summary = "Get elo matrix to show the possible ELO delta results of matches",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Success",
                            content = @Content(schema = @Schema(implementation = List.class))
                    )
            }
    )
    @GetMapping("/matrix-score")
    public ResponseEntity<List<Map<String, Object>>> getEloMatrix() {
        List<Map<String, Object>> matrix = eloMatrixService.generateEloMatrix();

        return ResponseEntity.ok(matrix);
    }

    @Operation(
            summary = "Get the loser score of a match with its associated ELO difference between both teams",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Success",
                            content = @Content(schema = @Schema(implementation = List.class))
                    )
            }
    )
    @GetMapping("/matrix-score/results")
    public ResponseEntity<List<MatrixScoreResultsResponseDTO>> getResultPerElo() {
        List<MatrixScoreResultsResponseDTO> results = kickerStatService.getResultPerDeltaElo();

        return ResponseEntity.ok(results);
    }

    @Operation(
            summary = "Get statistics about given player",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Success",
                            content = @Content(schema = @Schema(implementation = PlayerStatsResponseDTO.class))
                    )
            }
    )

    @GetMapping("/player/{id:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}}")
    public ResponseEntity<PlayerStatsResponseDTO> getPlayerStats(@PathVariable UUID id) {
        PlayerStatsResponseDTO playerStats = kickerStatService.getPlayerStats(id);

        return ResponseEntity.ok(playerStats);
    }
}
