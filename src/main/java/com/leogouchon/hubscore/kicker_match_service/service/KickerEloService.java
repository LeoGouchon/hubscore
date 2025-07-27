package com.leogouchon.hubscore.kicker_match_service.service;

import com.leogouchon.hubscore.kicker_match_service.entity.KickerMatches;

public interface KickerEloService {
    void calculateElo(KickerMatches match);
    void recalculateAllElo();
}
