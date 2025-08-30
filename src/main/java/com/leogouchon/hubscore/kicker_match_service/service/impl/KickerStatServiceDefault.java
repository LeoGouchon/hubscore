package com.leogouchon.hubscore.kicker_match_service.service.impl;

import com.leogouchon.hubscore.kicker_match_service.dto.SeasonStatsResponseDTO;
import com.leogouchon.hubscore.kicker_match_service.dto.SeasonsStatsResponseDTO;
import com.leogouchon.hubscore.kicker_match_service.repository.KickerEloSeasonalRepository;
import com.leogouchon.hubscore.kicker_match_service.repository.projection.GlobalStatsResponseProjection;
import com.leogouchon.hubscore.kicker_match_service.dto.GlobalStatsWithHistoryDTO;
import com.leogouchon.hubscore.kicker_match_service.repository.KickerMatchRepository;
import com.leogouchon.hubscore.kicker_match_service.repository.projection.LastKickerEloByDateProjection;
import com.leogouchon.hubscore.kicker_match_service.repository.projection.SeasonStatsProjection;
import com.leogouchon.hubscore.kicker_match_service.service.EloCalculatorService;
import com.leogouchon.hubscore.kicker_match_service.service.KickerStatService;
import org.springframework.beans.factory.annotation.Autowired;
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
    private final KickerEloSeasonalRepository kickerEloSeasonalRepository;
    private final EloCalculatorService eloCalculator;

    @Autowired
    public KickerStatServiceDefault(KickerMatchRepository kickerMatchRepository, KickerEloSeasonalRepository kickerEloSeasonalRepository, EloCalculatorService eloCalculator) {
        this.kickerMatchRepository = kickerMatchRepository;
        this.kickerEloSeasonalRepository = kickerEloSeasonalRepository;
        this.eloCalculator = eloCalculator;
    }

    @Override
    public List<GlobalStatsWithHistoryDTO> getGlobalStats() {
        List<GlobalStatsResponseProjection> rawStats = kickerMatchRepository.getGlobalKickerStats(null, null);
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

    @Override
    public List<GlobalStatsWithHistoryDTO> getSeasonStats(int year, int quarter) {
        List<GlobalStatsResponseProjection> rawStats = kickerEloSeasonalRepository.getSeasonPlayerStats(year, quarter);
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(System.currentTimeMillis());
        c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        List<LastKickerEloByDateProjection> eloLastWeek = kickerEloSeasonalRepository.getLatestKickerSeasonEloByDate(new java.sql.Timestamp(c.getTimeInMillis()), year, quarter);

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
            dto.setCurrentElo(stat.getCurrentElo() != null ? stat.getCurrentElo() : eloCalculator.getInitialELo());
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

    @Override
    public SeasonsStatsResponseDTO getSeasonsStats() {
        int nbSeasons = kickerEloSeasonalRepository.getNbSeasons();
        int totalMatches = kickerMatchRepository.getTotalMatches();
        int totalPlayers = kickerMatchRepository.getTotalPlayers();

        List<SeasonStatsProjection> seasonsStats = kickerEloSeasonalRepository.getSeasonsStats();

        List<SeasonStatsResponseDTO> seasonsStatsResponse = seasonsStats.stream().map(stat -> {
            SeasonStatsResponseDTO dto = new SeasonStatsResponseDTO();
            dto.setYear(stat.getYear());
            dto.setQuarter(stat.getQuarter());
            dto.setNbMatches(stat.getNbMatches());
            dto.setNbPlayers(stat.getNbPlayers());

            return dto;
        }).toList();

        return new SeasonsStatsResponseDTO(nbSeasons, totalMatches, totalPlayers, seasonsStatsResponse);
    }
}
