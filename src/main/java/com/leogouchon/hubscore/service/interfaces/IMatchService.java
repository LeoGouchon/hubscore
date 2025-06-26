package com.leogouchon.hubscore.service.interfaces;

import com.leogouchon.hubscore.dto.BatchSessionResponseDTO;
import com.leogouchon.hubscore.dto.MatchResponseDTO;
import com.leogouchon.hubscore.model.Matches;
import com.leogouchon.hubscore.type.MatchPoint;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;

public interface IMatchService {
    Matches createMatch(Long player1Id, Long player2Id, List<MatchPoint> pointsHistory, Integer finalScoreA, Integer finalScoreB);
    void deleteMatch(Long id);
    Page<Matches> getMatches(int page, int size, List<Long> playerIds, Long date);
    Optional<Matches> getMatch(Long id);
    Optional<MatchResponseDTO> getMatchResponseDTO(Long id);
    Page<BatchSessionResponseDTO> getMatchesSessionsQuickStats(int page, int size);
}
