package com.leogouchon.hubscore.testutil;

import com.leogouchon.hubscore.kicker_match_service.dto.KickerMatchRequestDTO;
import com.leogouchon.hubscore.kicker_match_service.dto.PublicKickerMatchRequestDTO;

import java.util.UUID;

public final class TestKickerMatchRequests {
    private TestKickerMatchRequests() {
    }

    public static KickerMatchRequestDTO singlesRequest(UUID playerAId, UUID playerBId, int scoreA, int scoreB) {
        KickerMatchRequestDTO request = new KickerMatchRequestDTO();
        request.setPlayer1AId(playerAId);
        request.setPlayer1BId(playerBId);
        request.setScoreA(scoreA);
        request.setScoreB(scoreB);
        return request;
    }

    public static PublicKickerMatchRequestDTO publicSinglesRequest(
            String code,
            UUID playerAId,
            UUID playerBId,
            int scoreA,
            int scoreB
    ) {
        PublicKickerMatchRequestDTO request = new PublicKickerMatchRequestDTO();
        request.setCode(code);
        request.setMatch(singlesRequest(playerAId, playerBId, scoreA, scoreB));
        return request;
    }
}
