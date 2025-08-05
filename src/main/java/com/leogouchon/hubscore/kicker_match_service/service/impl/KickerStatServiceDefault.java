package com.leogouchon.hubscore.kicker_match_service.service.impl;

import com.leogouchon.hubscore.kicker_match_service.repository.projection.GlobalStatsResponseProjection;
import com.leogouchon.hubscore.kicker_match_service.dto.GlobalStatsWithHistoryDTO;
import com.leogouchon.hubscore.kicker_match_service.repository.KickerMatchRepository;
import com.leogouchon.hubscore.kicker_match_service.repository.projection.LastKickerEloByDateProjection;
import com.leogouchon.hubscore.kicker_match_service.service.KickerStatService;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class KickerStatServiceDefault implements KickerStatService {
    private final KickerMatchRepository kickerMatchRepository;

    public KickerStatServiceDefault(KickerMatchRepository kickerMatchRepository) {
        this.kickerMatchRepository = kickerMatchRepository;
    }

    @Override
    public List<GlobalStatsWithHistoryDTO> getGlobalStats() {
        List<GlobalStatsResponseProjection> rawStats = kickerMatchRepository.getGlobalKickerStats();
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(System.currentTimeMillis());
        c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        List<LastKickerEloByDateProjection> eloLastWeek = kickerMatchRepository.getLatestKickerEloByDate(new java.sql.Timestamp(c.getTimeInMillis()));

        Map<UUID, LastKickerEloByDateProjection> eloLastWeekMap = eloLastWeek.stream()
                .collect(Collectors.toMap(LastKickerEloByDateProjection::getPlayerId, Function.identity()));

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
            dto.setRank(stat.getRank());

            LastKickerEloByDateProjection lastWeek = eloLastWeekMap.get(stat.getPlayerId());
            if (lastWeek != null) {
                dto.setEloLastWeek(lastWeek.getElo());
                dto.setRankLastWeek(lastWeek.getRank());
            }

            return dto;
        }).toList();

        return fullStats;
    }
}
