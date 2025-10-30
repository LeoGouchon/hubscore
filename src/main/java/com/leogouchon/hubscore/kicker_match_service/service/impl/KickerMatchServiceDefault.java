package com.leogouchon.hubscore.kicker_match_service.service.impl;

import com.leogouchon.hubscore.kicker_match_service.dto.KickerMatchResponseDTO;
import com.leogouchon.hubscore.kicker_match_service.entity.KickerElo;
import com.leogouchon.hubscore.kicker_match_service.entity.KickerEloSeasonal;
import com.leogouchon.hubscore.kicker_match_service.entity.KickerMatches;
import com.leogouchon.hubscore.kicker_match_service.repository.KickerEloRepository;
import com.leogouchon.hubscore.kicker_match_service.repository.KickerEloSeasonalRepository;
import com.leogouchon.hubscore.kicker_match_service.repository.KickerMatchRepository;
import com.leogouchon.hubscore.kicker_match_service.service.KickerEloSeasonalService;
import com.leogouchon.hubscore.kicker_match_service.service.KickerEloService;
import com.leogouchon.hubscore.kicker_match_service.service.KickerMatchService;
import com.leogouchon.hubscore.kicker_match_service.specification.KickerMatchSpecifications;
import com.leogouchon.hubscore.player_service.entity.Players;
import com.leogouchon.hubscore.player_service.service.PlayerService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class KickerMatchServiceDefault implements KickerMatchService {
    private final KickerMatchRepository matchRepository;
    private final KickerEloRepository kickerEloRepository;
    private final KickerEloSeasonalRepository kickerEloSeasonalRepository;
    private final PlayerService playerService;
    private final KickerEloService kickerEloService;
    private final KickerEloSeasonalService kickerEloSeasonalService;

    @Autowired
    public KickerMatchServiceDefault(KickerMatchRepository matchRepository, KickerEloRepository kickerEloRepository, KickerEloSeasonalRepository kickerEloSeasonalRepository, PlayerService playerService, KickerEloService kickerEloService, KickerEloSeasonalService kickerEloSeasonalService) {
        this.matchRepository = matchRepository;
        this.kickerEloRepository = kickerEloRepository;
        this.kickerEloSeasonalRepository = kickerEloSeasonalRepository;
        this.playerService = playerService;
        this.kickerEloService = kickerEloService;
        this.kickerEloSeasonalService = kickerEloSeasonalService;
    }

    @Transactional
    @Override
    public KickerMatches createMatch(UUID player1TeamAId, UUID player2TeamAId,
                                     UUID player1TeamBId, UUID player2TeamBId,
                                     Integer finalScoreA, Integer finalScoreB) {

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
                finalScoreB
        );

        matchRepository.save(match);

        kickerEloService.calculateElo(match);
        kickerEloSeasonalService.calculateElo(match);

        return match;
    }

    @Transactional
    @Override
    public void deleteMatch(UUID id) {
        if (matchRepository.existsById(id)) {
            matchRepository.deleteById(id);
        } else {
            throw new EntityNotFoundException("Match not found with id: " + id);
        }
    }

    public void recalculateElo() {
        kickerEloService.recalculateAllElo();
        kickerEloSeasonalService.recalculateAllElo();
    }


    public Page<KickerMatchResponseDTO> getMatches(int page, int size, List<UUID> playerIds, Long date, String dateOrder) {
        Specification<KickerMatches> filter = KickerMatchSpecifications.withFilters(playerIds, date, dateOrder);
        Pageable pageable = PageRequest.of(page, size);

        Page<KickerMatches> matchesPage = matchRepository.findAll(filter, pageable);
        List<UUID> matchIds = matchesPage.getContent().stream()
                .map(KickerMatches::getId)
                .toList();

        Map<UUID, KickerElo> eloMap = kickerEloRepository.findAllByMatchIdIn(matchIds).stream()
                .collect(Collectors.toMap(e -> e.getMatch().getId(), e -> e, (existing, duplicate) -> existing));
        Map<UUID, KickerEloSeasonal> eloSeasonalMap = kickerEloSeasonalRepository.findAllByMatchIdIn(matchIds).stream()
                .collect(Collectors.toMap(e -> e.getMatch().getId(), e -> e, (existing, duplicate) -> existing));

        List<KickerMatchResponseDTO> dtoList = matchesPage.getContent().stream()
                .map(m -> {
                    int eloChange = Optional.ofNullable(eloMap.get(m.getId()))
                            .map(match -> Math.abs(match.getEloChange()))
                            .orElse(0);
                    int eloSeasonalChange = Optional.ofNullable(eloSeasonalMap.get(m.getId()))
                            .map(match -> Math.abs(match.getEloChange()))
                            .orElse(0);
                    return new KickerMatchResponseDTO(m, eloChange, eloSeasonalChange);
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

            int eloChange = Optional.ofNullable(elo)
                    .map(e -> Math.abs(e.getEloChange()))
                    .orElse(0);

            int eloSeasonalChange = Optional.ofNullable(eloSeasonal)
                    .map(e -> Math.abs(e.getEloChange()))
                    .orElse(0);

            return new KickerMatchResponseDTO(m, eloChange, eloSeasonalChange);
        });
    }
}
