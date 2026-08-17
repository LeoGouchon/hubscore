package com.leogouchon.husbcore.kicker_match_service.service;

import com.leogouchon.husbcore.kicker_match_service.entity.KickerMatches;

import java.sql.Timestamp;

public interface KickerEloService {
    void calculateElo(KickerMatches match);

    void recalculateAllElo();

    void recalculateFromDate(Timestamp cutoff);
}
