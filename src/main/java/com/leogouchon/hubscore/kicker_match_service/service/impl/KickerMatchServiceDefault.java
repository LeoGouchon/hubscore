package com.leogouchon.hubscore.kicker_match_service.service.impl;

import com.leogouchon.hubscore.kicker_match_service.dto.KickerMatchResponseDTO;
import com.leogouchon.hubscore.kicker_match_service.dto.controller_params.PlayerFilterDTO;
import com.leogouchon.hubscore.kicker_match_service.entity.KickerElo;
import com.leogouchon.hubscore.kicker_match_service.entity.KickerEloSeasonal;
import com.leogouchon.hubscore.kicker_match_service.entity.KickerMatches;
import com.leogouchon.hubscore.kicker_match_service.repository.KickerEloRepository;
import com.leogouchon.hubscore.kicker_match_service.repository.KickerEloSeasonalRepository;
import com.leogouchon.hubscore.kicker_match_service.repository.KickerMatchRepository;
import com.leogouchon.hubscore.kicker_match_service.service.EloCalculatorService;
import com.leogouchon.hubscore.kicker_match_service.service.KickerEloService;
import com.leogouchon.hubscore.kicker_match_service.service.KickerMatchService;
import com.leogouchon.hubscore.kicker_match_service.service.PlayerMatchFactsViewService;
import com.leogouchon.hubscore.kicker_match_service.specification.KickerMatchSpecifications;
import com.leogouchon.hubscore.player_service.entity.Players;
import com.leogouchon.hubscore.player_service.service.PlayerService;
import com.leogouchon.hubscore.user_service.entity.Users;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class KickerMatchServiceDefault implements KickerMatchService {
    private final KickerMatchRepository matchRepository;
    private final KickerEloRepository kickerEloRepository;
    private final KickerEloSeasonalRepository kickerEloSeasonalRepository;
    private final PlayerService playerService;
    private final KickerEloService kickerEloService;
    private final KickerEloService kickerEloSeasonalService;
    private final PlayerMatchFactsViewService playerMatchFactsViewService;
    private final EloCalculatorService eloCalculator;

    @Autowired
    public KickerMatchServiceDefault(
            KickerMatchRepository matchRepository,
            KickerEloRepository kickerEloRepository,
            KickerEloSeasonalRepository kickerEloSeasonalRepository,
            PlayerService playerService,
            @Qualifier("globalEloService") KickerEloService kickerEloService,
            @Qualifier("seasonalEloService") KickerEloService kickerEloSeasonalService,
            PlayerMatchFactsViewService playerMatchFactsViewService,
            EloCalculatorService eloCalculator) {
        this.matchRepository = matchRepository;
        this.kickerEloRepository = kickerEloRepository;
        this.kickerEloSeasonalRepository = kickerEloSeasonalRepository;
        this.playerService = playerService;
        this.kickerEloService = kickerEloService;
        this.kickerEloSeasonalService = kickerEloSeasonalService;
        this.playerMatchFactsViewService = playerMatchFactsViewService;
        this.eloCalculator = eloCalculator;
    }

    @Transactional
    @Override
    public KickerMatches createMatch(UUID player1TeamAId, UUID player2TeamAId,
                                     UUID player1TeamBId, UUID player2TeamBId,
                                     Integer finalScoreA, Integer finalScoreB,
                                     Users createdByUser) {

        Optional<Players> playerA1 = playerService.getPlayer(player1TeamAId);
        Optional<Players> playerA2 = playerService.getPlayer(player2TeamAId);
        Optional<Players> playerB1 = playerService.getPlayer(player1TeamBId);
        Optional<Players> playerB2 = playerService.getPlayer(player2TeamBId);

        if (playerA1.isEmpty() || playerB1.isEmpty()) {
            throw new IllegalArgumentException("Player 1 from each team is required");
        }

        boolean is1v1 = playerA2.isEmpty() && playerB2.isEmpty();
        boolean is2v2 = playerA2.isPresent() && playerB2.isPresent();

        if (!is1v1 && !is2v2) {
            throw new IllegalArgumentException("Must have exactly two or four players (1v1 or 2v2)");
        }

        Set<Players> players = new HashSet<>();
        players.add(playerA1.get());
        players.add(playerB1.get());
        playerA2.ifPresent(players::add);
        playerB2.ifPresent(players::add);

        if (!(players.size() == 2 || players.size() == 4)) {
            throw new IllegalArgumentException("Players must be unique (2 or 4 distinct players)");
        }

        KickerMatches match = new KickerMatches(
                playerA1.get(),
                playerA2.orElse(null),
                playerB1.get(),
                playerB2.orElse(null),
                finalScoreA,
                finalScoreB,
                createdByUser
        );

        matchRepository.save(match);

        kickerEloService.calculateElo(match);
        kickerEloSeasonalService.calculateElo(match);
        playerMatchFactsViewService.refreshAfterCommit();

        return match;
    }

    @Transactional
    @Override
    public void deleteMatch(UUID id) {
        KickerMatches match = matchRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("match not found: " + id));

        Timestamp cutoff = match.getCreatedAt();

        matchRepository.delete(match);

        kickerEloService.recalculateFromDate(cutoff);
        kickerEloSeasonalService.recalculateFromDate(cutoff);
        playerMatchFactsViewService.refreshAfterCommit();
    }

    public void recalculateElo() {
        kickerEloService.recalculateAllElo();
        kickerEloSeasonalService.recalculateAllElo();
        playerMatchFactsViewService.refresh();
    }


    public Page<KickerMatchResponseDTO> getMatches(int page, int size, List<UUID> playerIds, PlayerFilterDTO playerFilterDTO, Long date, String dateOrder) {
        Specification<KickerMatches> filter = KickerMatchSpecifications.withFilters(playerIds, playerFilterDTO, date);

        Sort sort = Sort.by("createdAt");

        sort = "descend".equalsIgnoreCase(dateOrder)
                ? sort.descending()
                : sort.ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<KickerMatches> matchesPage = matchRepository.findAll(filter, pageable);
        List<UUID> matchIds = matchesPage.getContent().stream()
                .map(KickerMatches::getId)
                .toList();

        if (matchIds.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, matchesPage.getTotalElements());
        }

        List<KickerElo> elos = kickerEloRepository.findAllByMatchIdIn(matchIds);
        List<KickerEloSeasonal> seasonalElos = kickerEloSeasonalRepository.findAllByMatchIdIn(matchIds);

        Map<UUID, KickerElo> eloMap = elos.stream()
                .collect(Collectors.toMap(e -> e.getMatch().getId(), e -> e, (existing, duplicate) -> existing));
        Map<UUID, KickerEloSeasonal> eloSeasonalMap = seasonalElos.stream()
                .collect(Collectors.toMap(e -> e.getMatch().getId(), e -> e, (existing, duplicate) -> existing));
        Map<UUID, Map<UUID, Integer>> eloChangeByMatchId = elos.stream()
                .collect(Collectors.groupingBy(
                        e -> e.getMatch().getId(),
                        Collectors.toMap(e -> e.getPlayer().getId(), KickerElo::getEloChange)
                ));
        Map<UUID, Map<UUID, Integer>> globalEloBeforeMatchByMatchId = elos.stream()
                .collect(Collectors.groupingBy(
                        e -> e.getMatch().getId(),
                        Collectors.toMap(e -> e.getPlayer().getId(), KickerElo::getEloBeforeMatch)
                ));
        Map<UUID, Map<UUID, Integer>> seasonalEloBeforeMatchByMatchId = seasonalElos.stream()
                .collect(Collectors.groupingBy(
                        e -> e.getMatch().getId(),
                        Collectors.toMap(e -> e.getPlayer().getId(), KickerEloSeasonal::getEloBeforeMatch)
                ));

        List<KickerMatchResponseDTO> dtoList = matchesPage.getContent().stream()
                .map(m -> {
                    int eloChange = Optional.ofNullable(eloMap.get(m.getId()))
                            .map(match -> Math.abs(match.getEloChange()))
                            .orElse(0);
                    int eloSeasonalChange = Optional.ofNullable(eloSeasonalMap.get(m.getId()))
                            .map(match -> Math.abs(match.getEloChange()))
                            .orElse(0);
                    Map<UUID, Integer> eloChangeByPlayerId = eloChangeByMatchId.getOrDefault(m.getId(), Map.of());
                    Map<UUID, Integer> globalEloBeforeMatchByPlayerId = globalEloBeforeMatchByMatchId.getOrDefault(m.getId(), Map.of());
                    Map<UUID, Integer> seasonalEloBeforeMatchByPlayerId = seasonalEloBeforeMatchByMatchId.getOrDefault(m.getId(), Map.of());
                    Double winChanceTeamA = getWinChanceTeamA(m, globalEloBeforeMatchByPlayerId, seasonalEloBeforeMatchByPlayerId);
                    return new KickerMatchResponseDTO(
                            m,
                            eloChange,
                            eloSeasonalChange,
                            getTeamEloChange(m.getPlayer1A(), eloChangeByPlayerId),
                            getTeamEloChange(m.getPlayer1B(), eloChangeByPlayerId),
                            globalEloBeforeMatchByPlayerId,
                            seasonalEloBeforeMatchByPlayerId,
                            winChanceTeamA,
                            getWinChanceTeamB(winChanceTeamA)
                    );
                })
                .toList();

        return new PageImpl<>(dtoList, pageable, matchesPage.getTotalElements());
    }

    public Optional<KickerMatches> getMatch(UUID id) {
        return matchRepository.findById(id);
    }

    public Optional<KickerMatchResponseDTO> getMatchResponseDTO(UUID id) {
        return matchRepository.findById(id).map(m -> {
            List<KickerElo> elos = kickerEloRepository.findAllByMatchIdIn(List.of(m.getId()));
            List<KickerEloSeasonal> seasonalElos = kickerEloSeasonalRepository.findAllByMatchIdIn(List.of(m.getId()));

            KickerElo elo = elos.stream().findFirst().orElse(null);
            KickerEloSeasonal eloSeasonal = seasonalElos.stream().findFirst().orElse(null);
            Map<UUID, Integer> globalEloBeforeMatchByPlayerId = elos.stream()
                    .collect(Collectors.toMap(e -> e.getPlayer().getId(), KickerElo::getEloBeforeMatch));
            Map<UUID, Integer> seasonalEloBeforeMatchByPlayerId = seasonalElos.stream()
                    .collect(Collectors.toMap(e -> e.getPlayer().getId(), KickerEloSeasonal::getEloBeforeMatch));
            Map<UUID, Integer> eloChangeByPlayerId = elos.stream()
                    .collect(Collectors.toMap(e -> e.getPlayer().getId(), KickerElo::getEloChange));

            int eloChange = Optional.ofNullable(elo)
                    .map(e -> Math.abs(e.getEloChange()))
                    .orElse(0);

            int eloSeasonalChange = Optional.ofNullable(eloSeasonal)
                    .map(e -> Math.abs(e.getEloChange()))
                    .orElse(0);
            Double winChanceTeamA = getWinChanceTeamA(m, globalEloBeforeMatchByPlayerId, seasonalEloBeforeMatchByPlayerId);

            return new KickerMatchResponseDTO(
                    m,
                    eloChange,
                    eloSeasonalChange,
                    getTeamEloChange(m.getPlayer1A(), eloChangeByPlayerId),
                    getTeamEloChange(m.getPlayer1B(), eloChangeByPlayerId),
                    globalEloBeforeMatchByPlayerId,
                    seasonalEloBeforeMatchByPlayerId,
                    winChanceTeamA,
                    getWinChanceTeamB(winChanceTeamA)
            );
        });
    }

    private Integer getTeamEloChange(Players player, Map<UUID, Integer> eloChangeByPlayerId) {
        return eloChangeByPlayerId.get(player.getId());
    }

    private Double getWinChanceTeamA(
            KickerMatches match,
            Map<UUID, Integer> globalEloBeforeMatchByPlayerId,
            Map<UUID, Integer> seasonalEloBeforeMatchByPlayerId
    ) {
        Double teamAElo = getTeamWeightedElo(
                match.getPlayer1A(),
                match.getPlayer2A(),
                globalEloBeforeMatchByPlayerId,
                seasonalEloBeforeMatchByPlayerId
        );
        Double teamBElo = getTeamWeightedElo(
                match.getPlayer1B(),
                match.getPlayer2B(),
                globalEloBeforeMatchByPlayerId,
                seasonalEloBeforeMatchByPlayerId
        );

        if (teamAElo == null || teamBElo == null) {
            return null;
        }

        return eloCalculator.expectedResult(teamAElo, teamBElo);
    }

    private Double getWinChanceTeamB(Double winChanceTeamA) {
        if (winChanceTeamA == null) {
            return null;
        }

        return 1.0 - winChanceTeamA;
    }

    private Double getTeamWeightedElo(
            Players player1,
            Players player2,
            Map<UUID, Integer> globalEloBeforeMatchByPlayerId,
            Map<UUID, Integer> seasonalEloBeforeMatchByPlayerId
    ) {
        Double player1Elo = getPlayerWeightedElo(player1, globalEloBeforeMatchByPlayerId, seasonalEloBeforeMatchByPlayerId);

        if (player1Elo == null) {
            return null;
        }

        if (player2 == null) {
            return player1Elo;
        }

        Double player2Elo = getPlayerWeightedElo(player2, globalEloBeforeMatchByPlayerId, seasonalEloBeforeMatchByPlayerId);

        if (player2Elo == null) {
            return null;
        }

        return (player1Elo + player2Elo) / 2.0;
    }

    private Double getPlayerWeightedElo(
            Players player,
            Map<UUID, Integer> globalEloBeforeMatchByPlayerId,
            Map<UUID, Integer> seasonalEloBeforeMatchByPlayerId
    ) {
        int globalElo = Optional.ofNullable(globalEloBeforeMatchByPlayerId.get(player.getId()))
                .orElse(eloCalculator.getInitialELo());
        int seasonalElo = Optional.ofNullable(seasonalEloBeforeMatchByPlayerId.get(player.getId()))
                .orElse(eloCalculator.getInitialELo());

        return seasonalElo * 0.7 + globalElo * 0.3;
    }
}
