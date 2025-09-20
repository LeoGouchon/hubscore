package com.leogouchon.hubscore.squash_match_service.repository.projection;

public interface ScoreDistributionProjection {
    int getCount();
    int getWinScore();
    int getLoseScore();
}
