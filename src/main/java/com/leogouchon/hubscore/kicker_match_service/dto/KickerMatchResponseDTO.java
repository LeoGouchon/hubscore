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

    public KickerMatchResponseDTO(KickerMatches kickerMatch, Integer deltaElo, Integer deltaEloSeasonal) {
        this(
                kickerMatch,
                new KickerMatchMetrics(deltaElo, deltaEloSeasonal, null, null, null, null),
                EloBeforeMatchContext.empty()
        );
    }

    public KickerMatchResponseDTO(
            KickerMatches kickerMatch,
            KickerMatchMetrics metrics,
            EloBeforeMatchContext eloBeforeMatchContext
    ) {
        this.id = kickerMatch.getId();
        this.createdAt = kickerMatch.getCreatedAt();

        assignPlayers(kickerMatch, eloBeforeMatchContext);

        this.scoreA = kickerMatch.getScoreA();
        this.scoreB = kickerMatch.getScoreB();

        this.deltaElo = metrics.deltaElo();
        this.deltaEloSeasonal = metrics.deltaEloSeasonal();
        this.eloWinTeamA = metrics.eloWinTeamA();
        this.eloWinTeamB = metrics.eloWinTeamB();

        this.winChanceTeamA = metrics.winChanceTeamA();
        this.winChanceTeamB = metrics.winChanceTeamB();
    }

    private void assignPlayers(
            KickerMatches kickerMatch,
            EloBeforeMatchContext eloBeforeMatchContext
    ) {
        this.player1A = buildPlayerResponse(kickerMatch.getPlayer1A(), eloBeforeMatchContext);
        this.player2A = buildNullablePlayerResponse(kickerMatch.getPlayer2A(), eloBeforeMatchContext);
        this.player1B = buildPlayerResponse(kickerMatch.getPlayer1B(), eloBeforeMatchContext);
        this.player2B = buildNullablePlayerResponse(kickerMatch.getPlayer2B(), eloBeforeMatchContext);
    }

    private PlayerResponseDTO buildPlayerResponse(
            Players player,
            EloBeforeMatchContext eloBeforeMatchContext
    ) {
        return new PlayerResponseDTO(
                player,
                eloBeforeMatchContext.globalByPlayerId().get(player.getId()),
                eloBeforeMatchContext.seasonalByPlayerId().get(player.getId())
        );
    }

    private PlayerResponseDTO buildNullablePlayerResponse(
            Players player,
            EloBeforeMatchContext eloBeforeMatchContext
    ) {
        return player == null ? null : buildPlayerResponse(player, eloBeforeMatchContext);
    }

    public record EloBeforeMatchContext(
            Map<UUID, Integer> globalByPlayerId,
            Map<UUID, Integer> seasonalByPlayerId
    ) {
        public static EloBeforeMatchContext empty() {
            return new EloBeforeMatchContext(Map.of(), Map.of());
        }
    }

    public record KickerMatchMetrics(
            Integer deltaElo,
            Integer deltaEloSeasonal,
            Integer eloWinTeamA,
            Integer eloWinTeamB,
            Double winChanceTeamA,
            Double winChanceTeamB
    ) {
    }
}
