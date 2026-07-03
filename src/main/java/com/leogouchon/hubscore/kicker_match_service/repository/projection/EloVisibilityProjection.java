package com.leogouchon.hubscore.kicker_match_service.repository.projection;

import java.util.UUID;

public interface EloVisibilityProjection {
    UUID getMatchId();
    UUID getPlayerId();
}
