package com.leogouchon.hubscore.testutil;

import com.leogouchon.hubscore.kicker_match_service.entity.KickerMatches;
import com.leogouchon.hubscore.player_service.entity.Players;

import java.util.UUID;

public final class TestKickerMatches {
    private TestKickerMatches() {
    }

    public static KickerMatches match(
            UUID id,
            Players player1A,
            Players player2A,
            Players player1B,
            Players player2B,
            int scoreA,
            int scoreB
    ) {
        KickerMatches match = new KickerMatches(player1A, player2A, player1B, player2B, scoreA, scoreB, null);
        match.setId(id);
        return match;
    }

    public static KickerMatches singlesMatch(UUID id, Players playerA, Players playerB, int scoreA, int scoreB) {
        return match(id, playerA, null, playerB, null, scoreA, scoreB);
    }
}
