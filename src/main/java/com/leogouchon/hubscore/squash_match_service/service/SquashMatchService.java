package com.leogouchon.hubscore.squash_match_service.service;

import com.leogouchon.hubscore.squash_match_service.dto.BatchSessionResponseDTO;
import com.leogouchon.hubscore.squash_match_service.dto.SquashMatchResponseDTO;
import com.leogouchon.hubscore.squash_match_service.entity.SquashMatches;
import com.leogouchon.hubscore.common.type.MatchPoint;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SquashMatchService {
    SquashMatches createMatch(UUID player1Id, UUID player2Id, List<MatchPoint> pointsHistory, Integer finalScoreA, Integer finalScoreB);
    void deleteMatch(UUID id);
    Page<SquashMatches> getMatches(int page, int size, List<UUID> playerIds, Long date);
    Optional<SquashMatches> getMatch(UUID id);
    Optional<SquashMatchResponseDTO> getMatchResponseDTO(UUID id);
    Page<BatchSessionResponseDTO> getMatchesSessionsQuickStats(int page, int size);
}
