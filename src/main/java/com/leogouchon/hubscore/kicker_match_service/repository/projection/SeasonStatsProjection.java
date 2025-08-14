package com.leogouchon.hubscore.kicker_match_service.repository.projection;

public interface SeasonStatsProjection {
    Integer getYear();
    Integer getQuarter();
    Integer getNbMatches();
    Integer getNbPlayers();
}
