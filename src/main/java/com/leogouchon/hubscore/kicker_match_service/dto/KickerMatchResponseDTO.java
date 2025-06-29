package com.leogouchon.hubscore.kicker_match_service.dto;

import com.leogouchon.hubscore.kicker_match_service.entity.KickerMatches;
import com.leogouchon.hubscore.player_service.entity.Players;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class KickerMatchResponseDTO {
    private Long id;
    private Players playerA1;
    private Players playerA2;
    private Players playerB1;
    private Players playerB2;
    private int finalScoreA;
    private int finalScoreB;

    public KickerMatchResponseDTO(KickerMatches match) {
        this.id = match.getId();
        this.playerA1 = match.getPlayer1TeamA();
        this.playerA2 = match.getPlayer2TeamA();
        this.playerB1 = match.getPlayer1TeamB();
        this.playerB2 = match.getPlayer2TeamB();
        this.finalScoreA = match.getFinalScoreA();
        this.finalScoreB = match.getFinalScoreB();
    }
}
