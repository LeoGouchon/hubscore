package com.leogouchon.hubscore.squash_match_service.service;

import com.leogouchon.hubscore.squash_match_service.dto.BatchSessionResponseDTO;
import com.leogouchon.hubscore.squash_match_service.dto.SquashMatchResponseDTO;
import com.leogouchon.hubscore.squash_match_service.entity.SquashMatches;
import com.leogouchon.hubscore.common.type.MatchPoint;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;

public interface SquashMatchService {
    SquashMatches createMatch(Long player1Id, Long player2Id, List<MatchPoint> pointsHistory, Integer finalScoreA, Integer finalScoreB);
    void deleteMatch(Long id);
    Page<SquashMatches> getMatches(int page, int size, List<Long> playerIds, Long date);
    Optional<SquashMatches> getMatch(Long id);
    Optional<SquashMatchResponseDTO> getMatchResponseDTO(Long id);
    Page<BatchSessionResponseDTO> getMatchesSessionsQuickStats(int page, int size);
}
