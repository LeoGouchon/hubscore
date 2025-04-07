package com.leogouchon.squashapp.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MatchRequestDTO {
    private Long playerAId;
    private Long playerBId;
    private String pointsHistory;
    private Integer finalScoreA;
    private Integer finalScoreB;
}
