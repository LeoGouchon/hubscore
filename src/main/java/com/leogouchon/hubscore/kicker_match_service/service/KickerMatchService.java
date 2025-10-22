package com.leogouchon.hubscore.kicker_match_service.service;

import com.leogouchon.hubscore.kicker_match_service.dto.KickerMatchResponseDTO;
import com.leogouchon.hubscore.kicker_match_service.entity.KickerMatches;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface KickerMatchService {
    KickerMatches createMatch(@NotNull UUID player1TeamAId, @Nullable UUID player2TeamAId, @NotNull UUID player1TeamBId, @Nullable UUID player2TeamBId, Integer finalScoreA, Integer finalScoreB);

    void deleteMatch(UUID id);

    Page<KickerMatchResponseDTO> getMatches(int page, int size, List<UUID> playerIds, Long date, String dateOrder);

    Optional<KickerMatches> getMatch(UUID id);

    Optional<KickerMatchResponseDTO> getMatchResponseDTO(UUID id);

    void recalculateElo();
}
