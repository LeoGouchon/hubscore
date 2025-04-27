package com.leogouchon.squashapp.service.interfaces;

import com.leogouchon.squashapp.dto.MatchResponseDTO;
import com.leogouchon.squashapp.model.Matches;
import org.springframework.data.domain.Page;

import java.util.Optional;

public interface IMatchService {
    Matches createMatch(Long player1Id, Long player2Id, String pointsHistory, Integer finalScoreA, Integer finalScoreB);
    void deleteMatch(Long id);
    Page<Matches> getMatches(int page, int size);
    Optional<Matches> getMatch(Long id);
    Optional<MatchResponseDTO> getMatchResponseDTO(Long id);
}
