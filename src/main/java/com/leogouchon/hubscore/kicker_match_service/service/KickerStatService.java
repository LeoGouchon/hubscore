package com.leogouchon.hubscore.kicker_match_service.service;

import com.leogouchon.hubscore.kicker_match_service.dto.GlobalStatsResponseDTO;

import java.util.List;

public interface KickerStatService {
    List<GlobalStatsResponseDTO> getGlobalStats();
}
