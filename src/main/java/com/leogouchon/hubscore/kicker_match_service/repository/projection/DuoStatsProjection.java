package com.leogouchon.hubscore.kicker_match_service.repository.projection;

public interface DuoStatsProjection {

    String getPlayer1Id();

    String getPlayer2Id();

    String getP1FirstName();

    String getP1LastName();

    String getP2FirstName();

    String getP2LastName();

    int getMatches();

    int getWins();

    int getLosses();

    int getEloTotal();

    int getPlayerEloAvg();

    int getTeammateEloAvg();

    int getOpponentEloAvg();

    int getEloGainAvg();

    int getEloGainMax();

    int getEloGainMin();
}
