package com.leogouchon.hubscore.kicker_match_service.controller;


import com.leogouchon.hubscore.authenticate_service.service.AuthenticateService;
import com.leogouchon.hubscore.common.dto.PaginatedResponseDTO;
import com.leogouchon.hubscore.kicker_match_service.dto.KickerMatchRequestDTO;
import com.leogouchon.hubscore.kicker_match_service.dto.KickerMatchResponseDTO;
import com.leogouchon.hubscore.kicker_match_service.entity.KickerMatches;
import com.leogouchon.hubscore.kicker_match_service.service.KickerMatchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
@RequestMapping(value = "/api/v1/kicker/matches")
@Tag(name = "Kicker")
@Tag(name = "Match")
@Validated
public class KickerMatchController {

    private final KickerMatchService matchService;
    private final AuthenticateService authenticateService;

    @Autowired
    public KickerMatchController(KickerMatchService matchService, AuthenticateService authenticateService) {
        this.matchService = matchService;
        this.authenticateService = authenticateService;
    }

    @Operation(
            summary = "Return matches",
            description = "Return matches from the database",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Matches found"),
            }
    )
    @GetMapping
    public ResponseEntity<PaginatedResponseDTO<KickerMatchResponseDTO>> getMatches(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int size,
            @RequestParam(name = "playerIds", required = false) List<UUID> playerIds,
            @RequestParam(name = "date", required = false) Long date,
            @RequestParam(name = "dateOrder", required = false, defaultValue = "ascend") String dateOrder
    ) {
        Page<KickerMatchResponseDTO> matchesPage = matchService.getMatches(page, size, playerIds, date, dateOrder);
        PaginatedResponseDTO<KickerMatchResponseDTO> response = new PaginatedResponseDTO<>(
                matchesPage.getContent(),
                matchesPage.getNumber(),
                matchesPage.getTotalPages(),
                matchesPage.getTotalElements(),
                matchesPage.getSize()
        );
        return ResponseEntity.ok(response);
    }

    @ApiResponse(responseCode = "200", description = "Match with given id found")
    @ApiResponse(responseCode = "404", description = "Match not found", content = {@Content(schema = @Schema())})
    @GetMapping("/{id:[0-9]+}")
    public ResponseEntity<KickerMatches> getMatch(@PathVariable UUID id) {
        Optional<KickerMatches> match = matchService.getMatch(id);
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
    public ResponseEntity<KickerMatchResponseDTO> createMatch(@Valid @RequestBody KickerMatchRequestDTO matchRequest) {
        try {
            KickerMatches createdMatch = matchService.createMatch(
                    matchRequest.getPlayer1AId(),
                    matchRequest.getPlayer2AId(),
                    matchRequest.getPlayer1BId(),
                    matchRequest.getPlayer2BId(),
                    matchRequest.getScoreA(),
                    matchRequest.getScoreB()
            );
            URI location = URI.create("/api/v1/kicker/matches/" + createdMatch.getId());
            Optional<KickerMatchResponseDTO> match = matchService.getMatchResponseDTO(createdMatch.getId());
            return match.map(m -> ResponseEntity.created(location).body(m)).orElseGet(() -> ResponseEntity.badRequest().build());
        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @SecurityRequirement(name = "bearerAuth")
    @ApiResponse(responseCode = "204", description = "Match deleted successfully")
    @ApiResponse(responseCode = "404", description = "Match to delete not found", content = {@Content(schema = @Schema())})
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = {@Content(schema = @Schema())})
    @DeleteMapping("/{id:[0-9]+}")
    public ResponseEntity<Void> deleteMatch(@PathVariable UUID id) {
        matchService.deleteMatch(id);
        return ResponseEntity.noContent().build();
    }

    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/recalculate-elo")
    public ResponseEntity<?> recalculateElo(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpServletResponse.SC_UNAUTHORIZED).body("Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7);
        boolean isAdmin = authenticateService.isUserAdmin(token);
        if (!isAdmin) {
            return ResponseEntity.status(HttpServletResponse.SC_UNAUTHORIZED).body("Unauthorized");
        }
        matchService.recalculateElo();
        return ResponseEntity.ok("ELO recalculated for all matches");
    }
}

