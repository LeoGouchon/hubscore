package com.leogouchon.hubscore.kicker_match_service.service;

import com.leogouchon.hubscore.kicker_match_service.dto.GlobalStatsWithHistoryDTO;
import com.leogouchon.hubscore.kicker_match_service.dto.MatrixScoreResultsResponseDTO;
import com.leogouchon.hubscore.kicker_match_service.dto.SeasonsStatsResponseDTO;

import java.util.List;

public interface KickerStatService {
    List<GlobalStatsWithHistoryDTO> getGlobalStats();
    List<GlobalStatsWithHistoryDTO> getSeasonStats(int year, int quarter);
    SeasonsStatsResponseDTO getSeasonsStats();
    List<MatrixScoreResultsResponseDTO> getResultPerDeltaElo();
}
