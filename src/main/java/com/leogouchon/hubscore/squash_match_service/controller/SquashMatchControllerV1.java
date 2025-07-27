package com.leogouchon.hubscore.squash_match_service.controller;

import com.leogouchon.hubscore.common.dto.PaginatedResponseDTO;
import com.leogouchon.hubscore.squash_match_service.dto.BatchSessionResponseDTO;
import com.leogouchon.hubscore.squash_match_service.dto.OverallStatsResponseDTO;
import com.leogouchon.hubscore.squash_match_service.dto.SquashMatchRequestDTO;
import com.leogouchon.hubscore.squash_match_service.dto.SquashMatchResponseDTO;
import com.leogouchon.hubscore.squash_match_service.entity.SquashMatches;
import com.leogouchon.hubscore.squash_match_service.service.SquashMatchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
@RequestMapping(value = "/api/v1/squash/matches")
@Tag(name = "Squash - Match")
@Validated
public class SquashMatchControllerV1 {

    private final SquashMatchService matchService;

    @Autowired
    public SquashMatchControllerV1(SquashMatchService matchService) {
        this.matchService = matchService;
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Return matches",
            description = "Return matches from the database",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Matches found"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized", content = {@Content(schema = @Schema())})
            }
    )
    @GetMapping
    public ResponseEntity<PaginatedResponseDTO<SquashMatches>> getMatches(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int size,
            @RequestParam(name = "playerIds", required = false) List<UUID> playerIds,
            @RequestParam(name = "date", required = false) Long date
    ) {
        Page<SquashMatches> matchesPage = matchService.getMatches(page, size, playerIds, date);
        PaginatedResponseDTO<SquashMatches> response = new PaginatedResponseDTO<>(
                matchesPage.getContent(),
                matchesPage.getNumber(),
                matchesPage.getTotalPages(),
                matchesPage.getTotalElements(),
                matchesPage.getSize()
        );
        return ResponseEntity.ok(response);
    }

    @SecurityRequirement(name = "bearerAuth")
    @ApiResponse(responseCode = "200", description = "Match with given id found")
    @ApiResponse(responseCode = "404", description = "Match not found", content = {@Content(schema = @Schema())})
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = {@Content(schema = @Schema())})
    @GetMapping("/{id:[0-9]+}")
    public ResponseEntity<SquashMatches> getMatch(@PathVariable UUID id) {
        Optional<SquashMatches> match = matchService.getMatch(id);
        return match.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Create match",
            description = "Create a new match",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Match created"),
                    @ApiResponse(responseCode = "400", description = "Bad request", content = {@Content(schema = @Schema())}),
                    @ApiResponse(responseCode = "401", description = "Unauthorized", content = {@Content(schema = @Schema())})
            }
    )
    @PostMapping
    public ResponseEntity<SquashMatchResponseDTO> createMatch(@Valid @RequestBody SquashMatchRequestDTO matchRequest) {
        try {
            SquashMatches createdMatch = matchService.createMatch(
                    matchRequest.getPlayerAId(),
                    matchRequest.getPlayerBId(),
                    matchRequest.getPointsHistory(),
                    matchRequest.getFinalScoreA(),
                    matchRequest.getFinalScoreB()
            );
            URI location = URI.create("/api/v1/squash/matches/" + createdMatch.getId());
            Optional<SquashMatchResponseDTO> match = matchService.getMatchResponseDTO(createdMatch.getId());
            return match.map(m -> ResponseEntity.created(location).body(m)).orElseGet(() -> ResponseEntity.badRequest().build());
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    @SecurityRequirement(name = "bearerAuth")
    @ApiResponse(responseCode = "204", description = "Match deleted successfully")
    @ApiResponse(responseCode = "404", description = "Match to delete not found", content = {@Content(schema = @Schema())})
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = {@Content(schema = @Schema())})
    @DeleteMapping("/{id:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}}")
    public ResponseEntity<Void> deleteMatch(@PathVariable UUID id) {
        matchService.deleteMatch(id);
        return ResponseEntity.noContent().build();
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Return matches date sessions",
            description = "Return all matches dates from the database and also quick data about those sessions"
    )
    @ApiResponse(responseCode = "200", description = "Matches dates found")
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = {@Content(schema = @Schema())})
    @GetMapping("/sessions")
    public ResponseEntity<PaginatedResponseDTO<BatchSessionResponseDTO>> getMatchesDates(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int size
    ) {
        Page<BatchSessionResponseDTO> matchesDatesPage = matchService.getMatchesSessionsQuickStats(page, size);
        PaginatedResponseDTO<BatchSessionResponseDTO> response = new PaginatedResponseDTO<>(
                matchesDatesPage.getContent(),
                matchesDatesPage.getNumber(),
                matchesDatesPage.getTotalPages(),
                matchesDatesPage.getTotalElements(),
                matchesDatesPage.getSize()
        );
        return ResponseEntity.ok(response);
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Return global data",
            description = "Return overall stats from the database squash matches"
    )
    @ApiResponse(responseCode = "200", description = "Matches dates found")
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = {@Content(schema = @Schema())})
    @GetMapping("/overall")
    public ResponseEntity<OverallStatsResponseDTO> getOverallStats() {
        OverallStatsResponseDTO response = matchService.getOverallStats();
        return ResponseEntity.ok(response);
    }
}
