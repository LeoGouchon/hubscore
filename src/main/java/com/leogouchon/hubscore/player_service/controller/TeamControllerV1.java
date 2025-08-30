package com.leogouchon.hubscore.player_service.controller;

import com.leogouchon.hubscore.player_service.dto.TeamResponseDTO;
import com.leogouchon.hubscore.player_service.service.TeamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController

@RequestMapping("/api/v1/teams")
@Tag(name = "Team")
public class TeamControllerV1 {
    private final TeamService teamService;

    @Autowired
    public TeamControllerV1(TeamService teamService) {
        this.teamService = teamService;
    }

    @Operation(
            summary = "Return teams",
            description = "Return teams from the database",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Matches found"),
                    @ApiResponse(responseCode = "400", description = "Bad request", content = {@Content(schema = @Schema())}),
                    @ApiResponse(responseCode = "401", description = "Unauthorized", content = {@Content(schema = @Schema())})
            }
    )
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping
    public List<TeamResponseDTO> getTeams(@RequestParam(required = false, defaultValue = "true") boolean isKicker,
                                          @RequestParam(required = false, defaultValue = "true") boolean isSquash) {
        return teamService.getTeams(isKicker, isSquash);
    }
}
