package com.leogouchon.hubscore.kicker_match_service.service;

import com.leogouchon.hubscore.kicker_match_service.dto.GlobalStatsWithHistoryDTO;

import java.util.List;

public interface KickerStatService {
    List<GlobalStatsWithHistoryDTO> getGlobalStats();
}
