package com.leogouchon.hubscore.kicker_match_service.dto;

import com.leogouchon.hubscore.kicker_match_service.entity.KickerMatches;
import com.leogouchon.hubscore.player_service.dto.PlayerResponseDTO;
import com.leogouchon.hubscore.player_service.entity.Players;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
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
    private int deltaElo;
    private int deltaEloSeasonal;
    private Date createdAt;

    public KickerMatchResponseDTO(KickerMatches match, Integer deltaElo, Integer deltaEloSeasonal) {
        this.id = match.getId();
        this.player1A = new PlayerResponseDTO(match.getPlayer1A());
        if (match.getPlayer2A() != null) {
            this.player2A = new PlayerResponseDTO(match.getPlayer2A());
        }
        this.player1B = new PlayerResponseDTO(match.getPlayer1B());
        if (match.getPlayer2B() != null) {
            this.player2B = new PlayerResponseDTO(match.getPlayer2B());
        }
        this.scoreA = match.getScoreA();
        this.scoreB = match.getScoreB();
        this.deltaElo = deltaElo;
        this.deltaEloSeasonal = deltaEloSeasonal;
        this.createdAt = match.getCreatedAt();
    }
}
