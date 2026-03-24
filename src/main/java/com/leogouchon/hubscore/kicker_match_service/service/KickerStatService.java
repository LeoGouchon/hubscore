package com.leogouchon.hubscore.kicker_match_service.service;

import com.leogouchon.hubscore.kicker_match_service.dto.*;

import java.util.List;
import java.util.UUID;

public interface KickerStatService {
    List<GlobalStatsWithHistoryDTO> getGlobalStats();
    List<GlobalStatsWithHistoryDTO> getSeasonStats(int year, int quarter);
    SeasonsStatsResponseDTO getSeasonsStats();
    List<MatrixScoreResultsResponseDTO> getResultPerDeltaElo();
    PlayerStatsResponseDTO getPlayerStats(UUID id);
    List<DuoStatsDTO> getDuoStats();
}
