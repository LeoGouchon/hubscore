package com.leogouchon.hubscore.kicker_match_service.dto;

import com.leogouchon.hubscore.kicker_match_service.entity.KickerMatches;
import com.leogouchon.hubscore.player_service.dto.PlayerResponseDTO;
import com.leogouchon.hubscore.player_service.entity.Players;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
public class KickerMatchResponseDTO {
    private UUID id;
    private PlayerResponseDTO playerA1;
    private PlayerResponseDTO playerA2;
    private PlayerResponseDTO playerB1;
    private PlayerResponseDTO playerB2;
    private int finalScoreA;
    private int finalScoreB;

    public KickerMatchResponseDTO(KickerMatches match) {
        this.id = match.getId();
        this.playerA1 = new PlayerResponseDTO(match.getPlayer1A());
        if (match.getPlayer2A() != null) {
            this.playerA2 = new PlayerResponseDTO(match.getPlayer2A());
        }
        this.playerB1 = new PlayerResponseDTO(match.getPlayer1B());
        if (match.getPlayer2B() != null) {
            this.playerB2 = new PlayerResponseDTO(match.getPlayer2B());
        }
        this.finalScoreA = match.getScoreA();
        this.finalScoreB = match.getScoreB();
    }
}
