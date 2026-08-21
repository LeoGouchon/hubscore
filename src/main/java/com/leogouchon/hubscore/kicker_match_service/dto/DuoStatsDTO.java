package com.leogouchon.hubscore.kicker_match_service.dto;

import com.leogouchon.hubscore.player_service.dto.PlayerResponseDTO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DuoStatsDTO {
    private PlayerResponseDTO player1;
    private PlayerResponseDTO player2;

    private Long matches;
    private Long wins;
    private Long losses;

    private Long eloGainTotal;
    private Float player1EloAvg;
    private Float player2EloAvg;
    private Float opponentEloAvg;
    private Float eloGainAvg;
    private Float eloGainMax;
    private Float eloGainMin;
}
