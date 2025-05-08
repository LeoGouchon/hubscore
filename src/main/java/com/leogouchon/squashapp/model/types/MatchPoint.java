package com.leogouchon.squashapp.model.types;

import com.leogouchon.squashapp.enums.PlayerLetter;
import com.leogouchon.squashapp.enums.ServiceSide;
import lombok.*;

@Getter
@Setter
@Data
public class MatchPoint {
    private PlayerLetter server;
    private ServiceSide serviceSide;
    private PlayerLetter scorer;
    private int scoreA;
    private int scoreB;

    public MatchPoint(PlayerLetter server, ServiceSide serviceSide, PlayerLetter scorer, int scoreA, int scoreB) {
        this.server = server;
        this.serviceSide = serviceSide;
        this.scorer = scorer;
        this.scoreA = scoreA;
        this.scoreB = scoreB;
    }

    public MatchPoint() {}
}