package com.leogouchon.hubscore.kicker_match_service.service.impl;

import com.leogouchon.hubscore.kicker_match_service.service.EloCalculatorService;
import com.leogouchon.hubscore.player_service.entity.Players;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
public class EloCalculatorDefault implements EloCalculatorService {
    public int getInitialELo() {
        return 1500;
    }

    public int calculateDeltaElo(int K, double actualScore, double expectedScore) {
        return (int) Math.round(K * (actualScore - expectedScore));
    }

    public double averageElo(Players player1, Players player2, Map<UUID, Integer> currentElos) {
        if (player2 != null) {
            return (double) (currentElos.get(player1.getId()) + currentElos.get(player2.getId())) / 2;
        }
        return currentElos.get(player1.getId());
    }

    public int calculateK(int scoreDiff) {
        // Clamp scoreDiff between -10 and +10
        // Clamp entre 1 et 20
        int clamped = Math.max(1, Math.min(scoreDiff, 20));

        // Normalise sur [0, 1]
        double ratio = (clamped - 1) / 19.0;
        double exponent = 1;

        // K between [20, 40]
        double k = 20 + Math.pow(ratio, exponent) * (20 - 1);

        return (int) Math.round(k);
    }

    public double getScore(int scoreA, int scoreB) {
        return scoreA > scoreB ? 1 : scoreA < scoreB ? 0 : 0.5;
    }

    public double exceptedResult(double eloTeamA, double eloTeamB) {
        return  1 / (1 + Math.pow(10, (eloTeamB - eloTeamA) / 400.0)); // https://en.wikipedia.org/wiki/Elo_rating_system#cite_note-29
    }
}
