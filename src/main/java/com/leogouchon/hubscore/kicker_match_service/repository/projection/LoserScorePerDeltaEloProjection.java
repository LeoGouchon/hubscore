package com.leogouchon.husbcore.kicker_match_service.repository.projection;

public interface LoserScorePerDeltaEloProjection {
    double getEloDiff();

    int getLoserScore();
}
