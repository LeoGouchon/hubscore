package com.leogouchon.husbcore.kicker_match_service.repository.projection;

public interface SeasonStatsProjection {
    Integer getYear();

    Integer getQuarter();

    Integer getNbMatches();

    Integer getNbPlayers();
}
