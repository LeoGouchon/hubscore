package com.leogouchon.hubscore.kicker_match_service.service;

import com.leogouchon.hubscore.kicker_match_service.dto.KickerMatchResponseDTO;
import com.leogouchon.hubscore.kicker_match_service.entity.KickerMatches;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface KickerMatchService {
    KickerMatches createMatch(UUID player1TeamAId, UUID player2TeamAId, UUID player1TeamBId, UUID player2TeamBId, Integer finalScoreA, Integer finalScoreB);
    void deleteMatch(UUID id);
    Page<KickerMatches> getMatches(int page, int size, List<UUID> playerIds, Long date);
    Optional<KickerMatches> getMatch(UUID id);
    Optional<KickerMatchResponseDTO> getMatchResponseDTO(UUID id);
}
