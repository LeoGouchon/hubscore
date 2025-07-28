package com.leogouchon.hubscore.squash_match_service.repository.projection;

import java.sql.Timestamp;
import java.util.UUID;

public interface LightDataMatchProjection {
    UUID getId();

    int getFinalScoreA();

    int getFinalScoreB();

    UUID getPlayerAId();

    String getPlayerAFirstname();

    String getPlayerALastname();

    UUID getPlayerBId();

    String getPlayerBFirstname();

    String getPlayerBLastname();

    Timestamp getStartTime();
}
