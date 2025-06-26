package com.leogouchon.hubscore.squash_match_service.dto;

import com.leogouchon.hubscore.squash_match_service.entity.Matches;
import com.leogouchon.hubscore.player_service.entity.Players;
import com.leogouchon.hubscore.common.type.MatchPoint;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class MatchResponseDTO {
    private Long id;
    private Players playerA;
    private Players playerB;
    private List<MatchPoint> pointsHistory;
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
