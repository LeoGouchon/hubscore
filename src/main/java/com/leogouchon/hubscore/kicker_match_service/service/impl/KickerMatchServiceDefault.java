package com.leogouchon.hubscore.kicker_match_service.service.impl;

import com.leogouchon.hubscore.kicker_match_service.dto.KickerMatchResponseDTO;
import com.leogouchon.hubscore.kicker_match_service.entity.KickerMatches;
import com.leogouchon.hubscore.kicker_match_service.repository.KickerMatchRepository;
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

    @Autowired
    public KickerMatchServiceDefault(KickerMatchRepository matchRepository, PlayerService playerService) {
        this.matchRepository = matchRepository;
        this.playerService = playerService;
    }

    public KickerMatches createMatch(Long player1TeamAId, Long player2TeamAId, Long player1TeamBId, Long player2TeamBId, Integer finalScoreA, Integer finalScoreB) throws RuntimeException {
        Optional<Players> playerA1 = playerService.getPlayer(player1TeamAId);
        Optional<Players> playerA2 = playerService.getPlayer(player2TeamAId);
        Optional<Players> playerB1 = playerService.getPlayer(player1TeamBId);
        Optional<Players> playerB2 = playerService.getPlayer(player2TeamBId);

        if (playerA1.isEmpty() || playerB1.isEmpty()) {
            throw new IllegalArgumentException("Players must not be null");
        }
        else if (playerA1.equals(playerB1)) {
            throw new IllegalArgumentException("Players must be different");
        }
        else if ((playerA2.isEmpty() && playerB2.isPresent()) || (playerA2.isPresent() && playerB2.isEmpty())) {
            throw new IllegalArgumentException("Must have exactly two or four different players");
        } else if (new HashSet<>(List.of(playerA1.get(), playerA2.get(), playerB1.get(), playerB2.get())).size() == 4 || new HashSet<>(List.of(playerA1.get(), playerA2.get(), playerB1.get(), playerB2.get())).size() == 2) {
            KickerMatches match = new KickerMatches(playerA1.get(), playerA2.get(), playerB1.get(), playerB2.get());
            return matchRepository.save(match);
        } else {
            throw new IllegalArgumentException("Must have exactly two or four different players");
        }
    }

    public void deleteMatch(Long id) {
        if (matchRepository.existsById(id)) {
            matchRepository.deleteById(id);
        } else {
            throw new EntityNotFoundException("Match not found with id: " + id);
        }
    }

    public Page<KickerMatches> getMatches(int page, int size, List<Long> playerIds, Long date) {
        Specification<KickerMatches> filter = KickerMatchSpecifications.withFilters(playerIds, date);
        Pageable pageable = PageRequest.of(page, size);

        return matchRepository.findAll(filter, pageable);
    }

    public Optional<KickerMatches> getMatch(Long id) {
        return matchRepository.findById(id);
    }

    public Optional<KickerMatchResponseDTO> getMatchResponseDTO(Long id) {
        Optional<KickerMatches> match = matchRepository.findById(id);
        return match.map(KickerMatchResponseDTO::new);
    }
}
