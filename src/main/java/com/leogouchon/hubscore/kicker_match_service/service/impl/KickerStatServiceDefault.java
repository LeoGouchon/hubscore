package com.leogouchon.hubscore.kicker_match_service.service.impl;

import com.leogouchon.hubscore.kicker_match_service.dto.*;
import com.leogouchon.hubscore.kicker_match_service.repository.KickerEloRepository;
import com.leogouchon.hubscore.kicker_match_service.repository.KickerEloSeasonalRepository;
import com.leogouchon.hubscore.kicker_match_service.repository.KickerMatchRepository;
import com.leogouchon.hubscore.kicker_match_service.repository.projection.*;
import com.leogouchon.hubscore.kicker_match_service.service.KickerStatService;
import com.leogouchon.hubscore.player_service.dto.PlayerResponseDTO;
import com.leogouchon.hubscore.player_service.entity.Players;
import com.leogouchon.hubscore.player_service.service.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.leogouchon.hubscore.kicker_match_service.service.KickerEloVisibilityRules.MIN_RANKED_MATCHES;

@Service
public class KickerStatServiceDefault implements KickerStatService {
    private final KickerMatchRepository kickerMatchRepository;
    private final KickerEloRepository kickerEloRepository;
    private final KickerEloSeasonalRepository kickerEloSeasonalRepository;
    private final PlayerService playerService;

    @Autowired
    public KickerStatServiceDefault(KickerMatchRepository kickerMatchRepository, KickerEloSeasonalRepository kickerEloSeasonalRepository, PlayerService playerService, KickerEloRepository kickerEloRepository) {
        this.kickerMatchRepository = kickerMatchRepository;
        this.kickerEloSeasonalRepository = kickerEloSeasonalRepository;
        this.playerService = playerService;
        this.kickerEloRepository = kickerEloRepository;
    }

    @Override
    public List<GlobalStatsWithHistoryDTO> getGlobalStats() {
        List<GlobalStatsResponseProjection> rawStats = kickerMatchRepository.getGlobalKickerStats(null, null, MIN_RANKED_MATCHES);
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(System.currentTimeMillis());
        c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        List<LastKickerEloByDateProjection> eloLastWeek = kickerMatchRepository.getLatestKickerEloByDate(
                new java.sql.Timestamp(c.getTimeInMillis()),
                MIN_RANKED_MATCHES
        );

        Map<UUID, LastKickerEloByDateProjection> eloLastWeekMap = eloLastWeek.stream()
                .collect(Collectors.toMap(LastKickerEloByDateProjection::getPlayerId, Function.identity()));

        return rawStats.stream().map(stat -> {
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
    }

    @Override
    public List<GlobalStatsWithHistoryDTO> getSeasonStats(int year, int quarter) {
        List<GlobalStatsResponseProjection> rawStats = kickerEloSeasonalRepository.getSeasonPlayerStats(year, quarter, MIN_RANKED_MATCHES);
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(System.currentTimeMillis());
        c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        List<LastKickerEloByDateProjection> eloLastWeek = kickerEloSeasonalRepository.getLatestKickerSeasonEloByDate(
                new java.sql.Timestamp(c.getTimeInMillis()),
                year,
                quarter,
                MIN_RANKED_MATCHES
        );

        Map<UUID, LastKickerEloByDateProjection> eloLastWeekMap = eloLastWeek.stream()
                .collect(Collectors.toMap(LastKickerEloByDateProjection::getPlayerId, Function.identity()));

        return rawStats.stream().map(stat -> {
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
    }

    @Override
    public SeasonsStatsResponseDTO getSeasonsStats() {
        int nbSeasons = kickerEloSeasonalRepository.getNbSeasons();
        int totalMatches = kickerMatchRepository.getTotalMatches();
        int totalPlayers = kickerMatchRepository.getTotalPlayers();

        List<SeasonStatsProjection> seasonsStats = kickerEloSeasonalRepository.getSeasonsStats();

        List<SeasonStatsResponseDTO> seasonsStatsResponse = new ArrayList<>(seasonsStats.stream().map(stat -> {
            SeasonStatsResponseDTO dto = new SeasonStatsResponseDTO();
            dto.setYear(stat.getYear());
            dto.setQuarter(stat.getQuarter());
            dto.setNbMatches(stat.getNbMatches());
            dto.setNbPlayers(stat.getNbPlayers());

            return dto;
        }).toList());

        LocalDate today = currentDate();
        int currentYear = today.getYear();
        int currentQuarter = (today.getMonthValue() - 1) / 3 + 1;
        boolean currentSeasonExists = seasonsStatsResponse.stream()
                .anyMatch(stat -> stat.getYear() == currentYear && stat.getQuarter() == currentQuarter);

        if (!currentSeasonExists) {
            SeasonStatsResponseDTO currentSeason = new SeasonStatsResponseDTO();
            currentSeason.setYear(currentYear);
            currentSeason.setQuarter(currentQuarter);
            currentSeason.setNbMatches(0);
            currentSeason.setNbPlayers(0);
            seasonsStatsResponse.addFirst(currentSeason);
            nbSeasons++;
        }

        return new SeasonsStatsResponseDTO(nbSeasons, totalMatches, totalPlayers, seasonsStatsResponse);
    }

    protected LocalDate currentDate() {
        return LocalDate.now();
    }

    @Override
    public List<MatrixScoreResultsResponseDTO> getResultPerDeltaElo() {
        List<LoserScorePerDeltaEloProjection> results = kickerEloSeasonalRepository.getLoserScorePerEloDiff();

        return results.stream().map(result -> new MatrixScoreResultsResponseDTO(
                result.getEloDiff(),
                result.getLoserScore()
        )).toList();
    }

    @Override
    public PlayerStatsResponseDTO getPlayerStats(UUID id) {
        Optional<Players> player = playerService.getPlayer(id);

        if (player.isEmpty()) {
            throw new RuntimeException("Player not found");
        }

        List<PartnerStatsDTO> statsPerPartner = kickerMatchRepository.getPartnerStats(id);
        List<OpponentStatsDTO> statsPerOpponent = kickerMatchRepository.getOpponentStats(id);
        List<SeasonalStatsDTO> seasonalStats = kickerEloSeasonalRepository.getSeasonalStats(player.get().getId());
        for (SeasonalStatsDTO season : seasonalStats) {
            List<EloHistoryDTO> history = hasEnoughRankedMatches(season.getWins(), season.getLosses())
                    ? kickerEloSeasonalRepository.getEloHistory(player.get().getId(), season.getYear(), season.getQuarter())
                    : List.of();
            season.setEloHistory(history);
        }
        OverallStatsDTO allTimeStats = kickerMatchRepository.getAllTimeStats(id);
        List<EloHistoryDTO> history = hasEnoughRankedMatches(allTimeStats.getWins(), allTimeStats.getLosses())
                ? kickerEloRepository.getEloHistory(player.get().getId())
                : List.of();
        allTimeStats.setEloHistory(history);

        return new PlayerStatsResponseDTO(
                id,
                player.get().getFirstname(),
                player.get().getLastname(),
                statsPerPartner,
                statsPerOpponent,
                seasonalStats,
                allTimeStats
        );
    }

    private boolean hasEnoughRankedMatches(Long wins, Long losses) {
        return Optional.ofNullable(wins).orElse(0L) + Optional.ofNullable(losses).orElse(0L) >= MIN_RANKED_MATCHES;
    }

    @Override
    public List<DuoStatsDTO> getDuoStats() {
        List<DuoStatsProjection> duoStats = kickerMatchRepository.getDuoStats();

        return duoStats.stream().map(duo -> {
            DuoStatsDTO dto = new DuoStatsDTO();
            Players player1 = playerService.getPlayer(UUID.fromString(duo.getPlayer1Id())).get();
            Players player2 = playerService.getPlayer(UUID.fromString(duo.getPlayer2Id())).get();

            dto.setPlayer1(new PlayerResponseDTO(
                    player1.getId(),
                    player1.getFirstname(),
                    player1.getLastname()
            ));
            dto.setPlayer2(new PlayerResponseDTO(
                    player2.getId(),
                    player2.getFirstname(),
                    player2.getLastname()
            ));

            dto.setMatches((long) duo.getMatches());
            dto.setWins((long) duo.getWins());
            dto.setLosses((long) duo.getLosses());

            dto.setEloGainTotal((long) duo.getEloTotal());
            dto.setPlayer1EloAvg((float) duo.getPlayerEloAvg());
            dto.setPlayer2EloAvg((float) duo.getTeammateEloAvg());
            dto.setOpponentEloAvg((float) duo.getOpponentEloAvg());
            dto.setEloGainAvg((float) duo.getEloGainAvg());
            dto.setEloGainMax((float) duo.getEloGainMax());
            dto.setEloGainMin((float) duo.getEloGainMin());

            return dto;
        }).toList();
    }
}
