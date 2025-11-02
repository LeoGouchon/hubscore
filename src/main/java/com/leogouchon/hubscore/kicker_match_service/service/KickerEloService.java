package com.leogouchon.hubscore.kicker_match_service.service;

import com.leogouchon.hubscore.kicker_match_service.entity.KickerMatches;

import java.sql.Timestamp;

public interface KickerEloService {
    void calculateElo(KickerMatches match);
    void recalculateAllElo();
    void recalculateFromDate(Timestamp cutoff);
}
