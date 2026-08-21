package com.leogouchon.hubscore.kicker_match_service.service;

import java.util.List;
import java.util.Map;

public interface EloMatrixService {
    List<Map<String, Object>> generateEloMatrix();
}
