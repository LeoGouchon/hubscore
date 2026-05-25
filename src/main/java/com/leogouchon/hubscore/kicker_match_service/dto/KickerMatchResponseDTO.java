package com.leogouchon.hubscore.kicker_match_service.dto;

import com.leogouchon.hubscore.kicker_match_service.entity.KickerMatches;
import com.leogouchon.hubscore.player_service.dto.PlayerResponseDTO;
import com.leogouchon.hubscore.player_service.entity.Players;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Setter
@Getter
public class KickerMatchResponseDTO {
    private UUID id;
    private PlayerResponseDTO player1A;
    private PlayerResponseDTO player2A;
    private PlayerResponseDTO player1B;
    private PlayerResponseDTO player2B;
    private int scoreA;
    private int scoreB;
    @Schema(deprecated = true, description = "Deprecated: use eloWinTeamA and eloWinTeamB for signed team ELO changes.")
    private int deltaElo;
    @Schema(deprecated = true, description = "Deprecated: use eloWinTeamA and eloWinTeamB for signed team ELO changes.")
    private int deltaEloSeasonal;
    private Integer eloWinTeamA;
    private Integer eloWinTeamB;
    private Double winChanceTeamA;
    private Double winChanceTeamB;
    private Date createdAt;

    public KickerMatchResponseDTO(KickerMatches match, Integer deltaElo, Integer deltaEloSeasonal) {
        this(match, deltaElo, deltaEloSeasonal, null, null, Map.of(), Map.of(), null, null);
    }

    public KickerMatchResponseDTO(
            KickerMatches match,
            Integer deltaElo,
            Integer deltaEloSeasonal,
            Integer eloWinTeamA,
            Integer eloWinTeamB,
            Map<UUID, Integer> globalEloBeforeMatchByPlayerId,
            Map<UUID, Integer> seasonalEloBeforeMatchByPlayerId,
            Double winChanceTeamA,
            Double winChanceTeamB
    ) {
        this.id = match.getId();
        this.player1A = buildPlayerResponse(match.getPlayer1A(), globalEloBeforeMatchByPlayerId, seasonalEloBeforeMatchByPlayerId);
        if (match.getPlayer2A() != null) {
            this.player2A = buildPlayerResponse(match.getPlayer2A(), globalEloBeforeMatchByPlayerId, seasonalEloBeforeMatchByPlayerId);
        }
        this.player1B = buildPlayerResponse(match.getPlayer1B(), globalEloBeforeMatchByPlayerId, seasonalEloBeforeMatchByPlayerId);
        if (match.getPlayer2B() != null) {
            this.player2B = buildPlayerResponse(match.getPlayer2B(), globalEloBeforeMatchByPlayerId, seasonalEloBeforeMatchByPlayerId);
        }
        this.scoreA = match.getScoreA();
        this.scoreB = match.getScoreB();
        this.deltaElo = deltaElo;
        this.deltaEloSeasonal = deltaEloSeasonal;
        this.eloWinTeamA = eloWinTeamA;
        this.eloWinTeamB = eloWinTeamB;
        this.winChanceTeamA = winChanceTeamA;
        this.winChanceTeamB = winChanceTeamB;
        this.createdAt = match.getCreatedAt();
    }

    private PlayerResponseDTO buildPlayerResponse(
            Players player,
            Map<UUID, Integer> globalEloBeforeMatchByPlayerId,
            Map<UUID, Integer> seasonalEloBeforeMatchByPlayerId
    ) {
        return new PlayerResponseDTO(
                player,
                globalEloBeforeMatchByPlayerId.get(player.getId()),
                seasonalEloBeforeMatchByPlayerId.get(player.getId())
        );
    }
}
