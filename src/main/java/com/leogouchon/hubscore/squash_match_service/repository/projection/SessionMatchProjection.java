package com.leogouchon.hubscore.squash_match_service.repository.projection;

import java.util.UUID;

public interface SessionMatchProjection {
    long getDayUnix();
    UUID getPlayerAId();
    String getPlayerAFirstname();
    String getPlayerALastname();
    UUID getPlayerBId();
    String getPlayerBFirstname();
    String getPlayerBLastname();
    int getFinalScoreA();
    int getFinalScoreB();
}
