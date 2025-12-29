package com.leogouchon.hubscore.kicker_match_service.service;

import com.leogouchon.hubscore.player_service.entity.Players;

import java.util.Map;
import java.util.UUID;

public interface EloCalculatorService {
    int calculateDeltaElo(int k, double actualScore, double expectedScore);
    int calculateK(int scoreDiff);
    double averageElo(Players player1, Players player2, Map<UUID, Integer> currentElos);
    double getScore(int scoreA, int scoreB);
    double expectedResult(double eloTeamA, double eloTeamB);

    int getInitialELo();
}
