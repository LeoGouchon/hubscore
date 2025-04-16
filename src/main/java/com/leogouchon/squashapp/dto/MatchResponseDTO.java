package com.leogouchon.squashapp.dto;

import com.leogouchon.squashapp.model.Matches;
import com.leogouchon.squashapp.model.Players;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MatchResponseDTO {
    private Long id;
    private Players playerA;
    private Players playerB;
    private String pointsHistory;
    private int finalScoreA;
    private int finalScoreB;
    private boolean isFinished;

    public MatchResponseDTO(Matches match) {
        this.id = match.getId();
        this.playerA = match.getPlayerA();
        this.playerB = match.getPlayerB();
        this.pointsHistory = match.getPointsHistory();
        this.finalScoreA = match.getFinalScoreA();
        this.finalScoreB = match.getFinalScoreB();
    }
}
