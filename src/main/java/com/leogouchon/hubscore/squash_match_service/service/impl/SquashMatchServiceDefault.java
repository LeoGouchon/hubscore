package com.leogouchon.hubscore.squash_match_service.service.impl;

import com.leogouchon.hubscore.squash_match_service.dto.*;
import com.leogouchon.hubscore.squash_match_service.entity.SquashMatches;
import com.leogouchon.hubscore.player_service.entity.Players;
import com.leogouchon.hubscore.common.type.MatchPoint;
import com.leogouchon.hubscore.squash_match_service.repository.SquashMatchRepository;
import com.leogouchon.hubscore.squash_match_service.repository.projection.*;
import com.leogouchon.hubscore.squash_match_service.service.SquashMatchService;
import com.leogouchon.hubscore.player_service.service.PlayerService;
import com.leogouchon.hubscore.squash_match_service.specification.MatchSpecifications;
import com.leogouchon.hubscore.common.type.PlayerRank;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SquashMatchServiceDefault implements SquashMatchService {

    private final SquashMatchRepository matchRepository;
    private final PlayerService playerService;

    @Autowired
    public SquashMatchServiceDefault(SquashMatchRepository matchRepository, PlayerService playerService) {
        this.matchRepository = matchRepository;
        this.playerService = playerService;
    }

    public SquashMatches createMatch(UUID player1Id, UUID player2Id, List<MatchPoint> pointsHistory, Integer finalScoreA, Integer finalScoreB) throws RuntimeException {
        Optional<Players> playerA = playerService.getPlayer(player1Id);
        Optional<Players> playerB = playerService.getPlayer(player2Id);
        if (playerA.isEmpty() || playerB.isEmpty()) {
            throw new IllegalArgumentException("Given player(s) not found");
        } else {
            // TODO : verify points history
            if (pointsHistory != null) {
                SquashMatches match = new SquashMatches(playerA.get(), playerB.get(), pointsHistory, finalScoreA, finalScoreB);
                return matchRepository.save(match);
            } else if (finalScoreA != null && finalScoreB != null) {
                SquashMatches match = new SquashMatches(playerA.get(), playerB.get(), finalScoreA, finalScoreB);
                return matchRepository.save(match);
            } else {
                throw new IllegalArgumentException("Invalid parameters");
            }
        }
    }

    public void deleteMatch(UUID id) {
        if (matchRepository.existsById(id)) {
            matchRepository.deleteById(id);
        } else {
            throw new EntityNotFoundException("Player not found with id: " + id);
        }
    }

    public Page<SquashMatches> getMatches(int page, int size, List<UUID> playerIds, Long date) {
        Specification<SquashMatches> filter = MatchSpecifications.withFilters(playerIds, date);
        Pageable pageable = PageRequest.of(page, size);

        return matchRepository.findAll(filter, pageable);
    }

    public Optional<SquashMatches> getMatch(UUID id) {
        return matchRepository.findById(id);
    }

    public Optional<SquashMatchResponseDTO> getMatchResponseDTO(UUID id) {
        Optional<SquashMatches> match = matchRepository.findById(id);
        return match.map(SquashMatchResponseDTO::new);
    }

    public Page<BatchSessionResponseDTO> getMatchesSessionsQuickStats(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        List<SessionsDataProjection> rawResults = matchRepository.getSessionsData(pageable);

        Map<Long, List<SessionsDataProjection>> groupedByDay = rawResults.stream()
                .collect(Collectors.groupingBy(SessionsDataProjection::getDayUnix));

        List<BatchSessionResponseDTO> sessions = new ArrayList<>();

        for (Map.Entry<Long, List<SessionsDataProjection>> entry : groupedByDay.entrySet()) {
            Long dayUnix = entry.getKey();
            List<SessionsDataProjection> rows = entry.getValue();

            int matchCount = rows.stream().mapToInt(row -> ((Number) row.getWins()).intValue() + ((Number) row.getLosses()).intValue()).sum() / 2;

            List<PlayerRank> ranks = rows.stream().map(row -> {
                Players p = new Players();
                p.setId(row.getPlayerId());
                p.setFirstname(row.getPlayerName());

                PlayerRank rank = new PlayerRank();
                rank.setPlayer(p);
                rank.setWins(row.getWins());
                rank.setLosses(row.getLosses());
                rank.setTotalPointsScored(row.getPointsScored());
                rank.setTotalPointsReceived(row.getPointsConceded());
                return rank;
            }).toList();

            BatchSessionResponseDTO dto = new BatchSessionResponseDTO();
            dto.setDate(dayUnix);
            dto.setMatchCount(matchCount);
            dto.setRank(ranks.toArray(new PlayerRank[0]));

            sessions.add(dto);
        }

        sessions.sort(Comparator.comparing(BatchSessionResponseDTO::getDate).reversed());

        return new PageImpl<>(sessions, pageable, groupedByDay.size());
    }

    public OverallStatsResponseDTO getOverallStats() {
        List<Object[]> results = matchRepository.getOverallStats();
        List<LightDataMatchProjection> worstScoreOverall = matchRepository.getWorstScoreOverall(null);
        List<LightDataMatchProjection> closestScoreOverall = matchRepository.getClosestScoreOverall(null);
        List<ScoreDistributionProjection> scoreDistribution = matchRepository.getScoreDistribution();

        OverallStatsResponseDTO dto = new OverallStatsResponseDTO();
        dto.setTotalMatches(((Number) results.getFirst()[0]).intValue());
        dto.setAverageLoserScore(((BigDecimal) results.getFirst()[1]).doubleValue());
        dto.setCloseMatchesCount(((Number) results.getFirst()[2]).intValue());
        dto.setStompMatchesCount(((Number) results.getFirst()[3]).intValue());

        dto.setClosestMatches(closestScoreOverall.stream()
                .map(row -> new SquashMatchResponseDTO(
                        row.getId(),
                        new Players(row.getPlayerAId(), row.getPlayerAFirstname(), row.getPlayerALastname()),
                        new Players(row.getPlayerBId(), row.getPlayerBFirstname(), row.getPlayerBLastname()),
                        row.getFinalScoreA(),
                        row.getFinalScoreB(),
                        row.getStartTime()))
                .toArray(SquashMatchResponseDTO[]::new));
        dto.setStompestMatches(worstScoreOverall.stream()
                .map(row -> new SquashMatchResponseDTO(
                        row.getId(),
                        new Players(row.getPlayerAId(), row.getPlayerAFirstname(), row.getPlayerALastname()),
                        new Players(row.getPlayerBId(), row.getPlayerBFirstname(), row.getPlayerBLastname()),
                        row.getFinalScoreA(),
                        row.getFinalScoreB(),
                        row.getStartTime()))
                .toArray(SquashMatchResponseDTO[]::new));
        dto.setScoreDistribution(scoreDistribution.stream()
                .map(row -> new SquashScoreDistributionDTO(
                        row.getCount(),
                        row.getWinScore(),
                        row.getLoseScore()
                ))
                .toArray(SquashScoreDistributionDTO[]::new));

        return dto;
    }

    public PlayerStatsResponseDTO getPlayerStats(UUID playerId) {
        List<PlayerStatsProjection> globalStats = matchRepository.getStatsByPlayerId(playerId);
        List<OpponentStatsProjection> opponentStats = matchRepository.getDetailedStatsAgainstEachOpponent(playerId);

        opponentStats.forEach(opponentStat ->
                System.out.println(opponentStat.getOpponentId() + " - " + opponentStat.getOpponentFirstname() + " " + opponentStat.getOpponentLastname())
        );

        if (globalStats.isEmpty()) {
            throw new IllegalArgumentException("Aucune statistique trouvée pour le joueur avec l'ID : " + playerId);
        }
        PlayerStatsResponseDTO dto = new PlayerStatsResponseDTO();
        dto.setPlayer(new Players(playerId, globalStats.getFirst().getFirstname(), globalStats.getFirst().getLastname()));
        dto.setTotalMatches(globalStats.stream().mapToInt(PlayerStatsProjection::getTotalMatches).sum());
        dto.setWins(globalStats.stream().mapToInt(PlayerStatsProjection::getWins).sum());
        dto.setLosses(globalStats.stream().mapToInt(PlayerStatsProjection::getLosses).sum());
        dto.setAverageOpponentLostScore(globalStats.stream().mapToDouble(PlayerStatsProjection::getAverageOpponentLostScore).sum());
        dto.setAveragePlayerLostScore(globalStats.stream().mapToDouble(PlayerStatsProjection::getAveragePlayerLostScore).sum());
        dto.setCloseWonCount(globalStats.stream().mapToInt(PlayerStatsProjection::getCloseMatchesWonCount).sum());
        dto.setCloseLostCount(globalStats.stream().mapToInt(PlayerStatsProjection::getCloseMatchesLostCount).sum());
        dto.setStompWonCount(globalStats.stream().mapToInt(PlayerStatsProjection::getStompMatchesWonCount).sum());
        dto.setStompLostCount(globalStats.stream().mapToInt(PlayerStatsProjection::getStompMatchesLostCount).sum());
        dto.setStatsAgainstOpponents(opponentStats.stream()
                .map(opponent -> new StatsAgainstOpponentDTO(
                        new Players(opponent.getOpponentId(), opponent.getOpponentFirstname(), opponent.getOpponentLastname()),
                        opponent.getTotalMatches(),
                        opponent.getWins(),
                        opponent.getLosses(),
                        opponent.getAverageScoreWhenLost(),
                        opponent.getCloseWonCount(),
                        opponent.getCloseLostCount(),
                        opponent.getStompsWonCount(),
                        opponent.getStompsLostCount()
                ))
                .toArray(StatsAgainstOpponentDTO[]::new));

        return dto;
    }
}
