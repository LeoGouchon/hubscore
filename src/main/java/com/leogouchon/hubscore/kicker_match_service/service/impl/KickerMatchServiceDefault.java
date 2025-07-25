package com.leogouchon.hubscore.kicker_match_service.service.impl;

import com.leogouchon.hubscore.kicker_match_service.dto.KickerMatchResponseDTO;
import com.leogouchon.hubscore.kicker_match_service.entity.KickerMatches;
import com.leogouchon.hubscore.kicker_match_service.repository.KickerMatchRepository;
import com.leogouchon.hubscore.kicker_match_service.service.KickerEloService;
import com.leogouchon.hubscore.kicker_match_service.service.KickerMatchService;
import com.leogouchon.hubscore.kicker_match_service.specification.KickerMatchSpecifications;
import com.leogouchon.hubscore.player_service.entity.Players;
import com.leogouchon.hubscore.player_service.service.PlayerService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class KickerMatchServiceDefault implements KickerMatchService {
    private final KickerMatchRepository matchRepository;
    private final PlayerService playerService;
    private final KickerEloService kickerEloService;

    @Autowired
    public KickerMatchServiceDefault(KickerMatchRepository matchRepository, PlayerService playerService, KickerEloService kickerEloService) {
        this.matchRepository = matchRepository;
        this.playerService = playerService;
        this.kickerEloService = kickerEloService;
    }

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

        kickerEloService.calculateElo(match);

        return matchRepository.save(match);
    }

    public void deleteMatch(UUID id) {
        if (matchRepository.existsById(id)) {
            matchRepository.deleteById(id);
        } else {
            throw new EntityNotFoundException("Match not found with id: " + id);
        }
    }

    public Page<KickerMatches> getMatches(int page, int size, List<UUID> playerIds, Long date, String dateOrder) {
        Specification<KickerMatches> filter = KickerMatchSpecifications.withFilters(playerIds, date, dateOrder);
        Pageable pageable = PageRequest.of(page, size);

        return matchRepository.findAll(filter, pageable);
    }

    public Optional<KickerMatches> getMatch(UUID id) {
        return matchRepository.findById(id);
    }

    public Optional<KickerMatchResponseDTO> getMatchResponseDTO(UUID id) {
        Optional<KickerMatches> match = matchRepository.findById(id);
        return match.map(KickerMatchResponseDTO::new);
    }
}
