package com.leogouchon.hubscore.kicker_match_service.service.impl;

import com.leogouchon.hubscore.kicker_match_service.service.EloMatrixService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EloMatrixServiceDefault implements EloMatrixService {
    private final EloCalculatorDefault eloCalculator;

    @Autowired
    public EloMatrixServiceDefault(EloCalculatorDefault eloCalculator) {
        this.eloCalculator = eloCalculator;
    }

    @Override
    public List<Map<String, Object>> generateEloMatrix() {
        List<Map<String, Object>> matrix = new ArrayList<>();

        int baseElo = 1500;
        int maxDiff = 600;
        int step = 100;

        for (int eloDiff = -maxDiff; eloDiff <= maxDiff; eloDiff += step) {
            double eloA = baseElo;
            double eloB = (baseElo + eloDiff);

            double expectedA = eloCalculator.exceptedResult(eloA, eloB);

            for (int scoreDiff = 1; scoreDiff <= 20; scoreDiff++) {
                int k = eloCalculator.calculateK(scoreDiff);

                double win = eloCalculator.calculateDeltaElo(k, eloCalculator.getScore(10, 10 - scoreDiff), expectedA);
                double lose = eloCalculator.calculateDeltaElo(k, eloCalculator.getScore(10 - scoreDiff, 10), expectedA);

                Map<String, Object> row = new HashMap<>();
                row.put("eloDiff", eloDiff);
                row.put("scoreDiff", scoreDiff);
                row.put("deltaWin", win);
                row.put("deltaLose", lose);

                matrix.add(row);
            }
        }
        return matrix;
    }
}
