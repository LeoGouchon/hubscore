package com.leogouchon.hubscore.kicker_match_service.service.impl;

import com.leogouchon.hubscore.kicker_match_service.dto.GlobalStatsResponseDTO;
import com.leogouchon.hubscore.kicker_match_service.dto.GlobalStatsWithHistoryDTO;
import com.leogouchon.hubscore.kicker_match_service.repository.KickerMatchRepository;
import com.leogouchon.hubscore.kicker_match_service.service.KickerStatService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class KickerStatServiceDefault implements KickerStatService {
    private final KickerMatchRepository kickerMatchRepository;

    public KickerStatServiceDefault(KickerMatchRepository kickerMatchRepository) {
        this.kickerMatchRepository = kickerMatchRepository;
    }

    @Override
    public List<GlobalStatsWithHistoryDTO> getGlobalStats() {
        List<GlobalStatsResponseDTO> rawStats = kickerMatchRepository.getGlobalKickerStats();

        List<GlobalStatsWithHistoryDTO> fullStats = rawStats.stream().map(stat -> {
            List<Boolean> history = kickerMatchRepository.getLastFiveResultsByPlayerId(stat.getPlayerId());

            GlobalStatsWithHistoryDTO dto = new GlobalStatsWithHistoryDTO();
            dto.setPlayerId(stat.getPlayerId());
            dto.setFirstname(stat.getFirstname());
            dto.setLastname(stat.getLastname());
            dto.setTotalMatches(stat.getTotalMatches());
            dto.setWins(stat.getWins());
            dto.setLosses(stat.getLosses());
            dto.setWinRate(stat.getWinRate());
            dto.setLastMatches(history);
            dto.setCurrentElo(stat.getCurrentElo());

            return dto;
        }).toList();

        return fullStats;
    }
}
