package com.leogouchon.hubscore.common.type;

import com.leogouchon.hubscore.common.enums.PlayerLetter;
import com.leogouchon.hubscore.common.enums.ServiceSide;
import lombok.*;

import java.sql.Timestamp;

@Getter
@Setter
@Data
public class MatchPoint {
    private PlayerLetter server;
    private ServiceSide serviceSide;
    private PlayerLetter scorer;
    private int scoreA;
    private int scoreB;
    private Timestamp createdAt;

    public MatchPoint(PlayerLetter server, ServiceSide serviceSide, PlayerLetter scorer, int scoreA, int scoreB, Timestamp createdAt) {
        this.server = server;
        this.serviceSide = serviceSide;
        this.scorer = scorer;
        this.scoreA = scoreA;
        this.scoreB = scoreB;
        this.createdAt = createdAt;
    }

    public MatchPoint() {}
}
