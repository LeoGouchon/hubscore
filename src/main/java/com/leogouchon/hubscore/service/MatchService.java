package com.leogouchon.hubscore.service;

import com.leogouchon.hubscore.dto.BatchSessionResponseDTO;
import com.leogouchon.hubscore.dto.MatchResponseDTO;
import com.leogouchon.hubscore.model.Matches;
import com.leogouchon.hubscore.model.Players;
import com.leogouchon.hubscore.type.MatchPoint;
import com.leogouchon.hubscore.repository.MatchRepository;
import com.leogouchon.hubscore.service.interfaces.IMatchService;
import com.leogouchon.hubscore.service.interfaces.IPlayerService;
import com.leogouchon.hubscore.specification.MatchSpecifications;
import com.leogouchon.hubscore.type.PlayerRank;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

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

    public Page<BatchSessionResponseDTO> getMatchesSessionsQuickStats(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        List<Object[]> rawResults = matchRepository.getSessionsData(pageable);

        Map<Long, List<Object[]>> groupedByDay = rawResults.stream()
                .collect(Collectors.groupingBy(row -> (Long) row[0]));

        List<BatchSessionResponseDTO> sessions = new ArrayList<>();

        for (Map.Entry<Long, List<Object[]>> entry : groupedByDay.entrySet()) {
            Long dayUnix = entry.getKey();
            List<Object[]> rows = entry.getValue();

            int matchCount = ((Number) rows.getFirst()[1]).intValue();

            List<PlayerRank> ranks = rows.stream().map(row -> {
                Players p = new Players();
                p.setId(((Number) row[2]).longValue());
                p.setFirstname((String) row[3]);

                PlayerRank rank = new PlayerRank();
                rank.setPlayer(p);
                rank.setWins(((Number) row[4]).intValue());
                rank.setLosses(((Number) row[5]).intValue());
                rank.setTotalPointsScored(((Number) row[6]).intValue());
                rank.setTotalPointsReceived(((Number) row[7]).intValue());
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
}
