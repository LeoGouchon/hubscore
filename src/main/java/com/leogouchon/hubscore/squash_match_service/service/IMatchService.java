package com.leogouchon.hubscore.squash_match_service.service;

import com.leogouchon.hubscore.squash_match_service.dto.BatchSessionResponseDTO;
import com.leogouchon.hubscore.squash_match_service.dto.MatchResponseDTO;
import com.leogouchon.hubscore.squash_match_service.entity.Matches;
import com.leogouchon.hubscore.common.type.MatchPoint;
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
