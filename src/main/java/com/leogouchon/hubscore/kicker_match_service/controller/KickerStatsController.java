package com.leogouchon.hubscore.kicker_match_service.controller;

import com.leogouchon.hubscore.kicker_match_service.dto.SeasonsStatsResponseDTO;
import com.leogouchon.hubscore.kicker_match_service.dto.GlobalStatsWithHistoryDTO;
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

@RestController
@RequestMapping(value = "/api/v1/kicker/stats")
@Tag(name = "Kicker")
@Tag(name = "Stats")
@Validated
public class KickerStatsController {

    private final KickerStatService kickerStatService;

    public KickerStatsController(KickerStatService kickerStatService) {
        this.kickerStatService = kickerStatService;
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
}
