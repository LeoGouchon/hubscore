package com.leogouchon.squashapp.service;

import com.leogouchon.squashapp.dto.MatchResponseDTO;
import com.leogouchon.squashapp.model.Matches;
import com.leogouchon.squashapp.model.Players;
import com.leogouchon.squashapp.model.types.MatchPoint;
import com.leogouchon.squashapp.repository.MatchRepository;
import com.leogouchon.squashapp.service.interfaces.IMatchService;
import com.leogouchon.squashapp.service.interfaces.IPlayerService;
import com.leogouchon.squashapp.specification.MatchSpecifications;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Service
public class MatchService implements IMatchService {

    private final MatchRepository matchRepository;
    private final IPlayerService playerService;

    @Autowired
    public MatchService(MatchRepository matchRepository, IPlayerService playerService) {
        this.matchRepository = matchRepository;
        this.playerService = playerService;
    }

    public Matches createMatch(Long player1Id, Long player2Id, List<MatchPoint> pointsHistory, Integer finalScoreA, Integer finalScoreB) throws RuntimeException {
        Optional<Players> playerA = playerService.getPlayer(player1Id);
        Optional<Players> playerB = playerService.getPlayer(player2Id);
        if (playerA.isEmpty() || playerB.isEmpty()) {
            throw new IllegalArgumentException("Given player(s) not found");
        } else {
            // TODO : verify points history
            if (pointsHistory != null) {
                Matches match = new Matches(playerA.get(), playerB.get(), pointsHistory, finalScoreA, finalScoreB);
                return matchRepository.save(match);
            } else if (finalScoreA != null && finalScoreB != null) {
                Matches match = new Matches(playerA.get(), playerB.get(), finalScoreA, finalScoreB);
                return matchRepository.save(match);
            } else {
                throw new IllegalArgumentException("Invalid parameters");
            }
        }
    }

    public void deleteMatch(Long id) {
        if (matchRepository.existsById(id)) {
            matchRepository.deleteById(id);
        } else {
            throw new EntityNotFoundException("Player not found with id: " + id);
        }
    }

    public Page<Matches> getMatches(int page, int size, List<Long> playerIds, Long date) {
        Specification<Matches> filter = MatchSpecifications.withFilters(playerIds, date);
        Pageable pageable = PageRequest.of(page, size);

        return matchRepository.findAll(filter, pageable);
    }

    public Optional<Matches> getMatch(Long id) {
        return matchRepository.findById(id);
    }

    public Optional<MatchResponseDTO> getMatchResponseDTO(Long id) {
        Optional<Matches> match = matchRepository.findById(id);
        return match.map(MatchResponseDTO::new);
    }

    public Page<Timestamp> getMatchesDates(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        return matchRepository.getDates(pageable);
    }
}
