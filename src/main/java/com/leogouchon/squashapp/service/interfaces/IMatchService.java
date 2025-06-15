package com.leogouchon.squashapp.service.interfaces;

import com.leogouchon.squashapp.dto.MatchResponseDTO;
import com.leogouchon.squashapp.model.Matches;
import com.leogouchon.squashapp.model.types.MatchPoint;
import org.springframework.data.domain.Page;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

public interface IMatchService {
    Matches createMatch(Long player1Id, Long player2Id, List<MatchPoint> pointsHistory, Integer finalScoreA, Integer finalScoreB);
    void deleteMatch(Long id);
    Page<Matches> getMatches(int page, int size, List<Long> playerIds, Long date);
    Optional<Matches> getMatch(Long id);
    Optional<MatchResponseDTO> getMatchResponseDTO(Long id);
    Page<Timestamp> getMatchesDates(int page, int size);
}
