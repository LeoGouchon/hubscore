package com.leogouchon.hubscore.kicker_match_service.service.impl;

import com.leogouchon.hubscore.kicker_match_service.dto.GlobalStatsResponseDTO;
import com.leogouchon.hubscore.kicker_match_service.repository.KickerMatchRepository;
import com.leogouchon.hubscore.kicker_match_service.service.KickerStatService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class KickerStatServiceDefault implements KickerStatService {
    private final KickerMatchRepository kickerMatchRepository;

    public KickerStatServiceDefault(KickerMatchRepository kickerMatchRepository) {
        this.kickerMatchRepository = kickerMatchRepository;
    }

    @Override
    public List<GlobalStatsResponseDTO> getGlobalStats() {

        return kickerMatchRepository.getGlobalKickerStats();
    }
}
