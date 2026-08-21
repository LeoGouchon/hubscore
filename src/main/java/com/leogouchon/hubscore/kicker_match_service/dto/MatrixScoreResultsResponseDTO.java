package com.leogouchon.hubscore.kicker_match_service.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MatrixScoreResultsResponseDTO {
    private double eloDelta;
    private int score;

    public MatrixScoreResultsResponseDTO(double eloDelta, int score) {
        this.eloDelta = eloDelta;
        this.score = score;
    }
}
