package com.leogouchon.hubscore.kicker_match_service.service;

import com.leogouchon.hubscore.kicker_match_service.dto.KickerMatchResponseDTO;
import com.leogouchon.hubscore.kicker_match_service.entity.KickerMatches;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;

public interface KickerMatchService {
    KickerMatches createMatch(Long player1TeamAId, Long player2TeamAId, Long player1TeamBId, Long player2TeamBId, Integer finalScoreA, Integer finalScoreB);
    void deleteMatch(Long id);
    Page<KickerMatches> getMatches(int page, int size, List<Long> playerIds, Long date);
    Optional<KickerMatches> getMatch(Long id);
    Optional<KickerMatchResponseDTO> getMatchResponseDTO(Long id);
}
