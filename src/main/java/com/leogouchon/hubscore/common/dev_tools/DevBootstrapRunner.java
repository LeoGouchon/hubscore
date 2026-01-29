package com.leogouchon.hubscore.common.dev_tools;

import com.leogouchon.hubscore.kicker_match_service.service.KickerMatchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
@DependsOn("flyway")
@Slf4j
public class DevBootstrapRunner {

    private final KickerMatchService kickerMatchService;
    private final JdbcTemplate jdbcTemplate;

    public DevBootstrapRunner(
            KickerMatchService kickerMatchService,
            JdbcTemplate jdbcTemplate
    ) {
        this.kickerMatchService = kickerMatchService;
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Triggered by an ApplicationReadyEvent, this method runs ELO recalculation
     * and refreshes the materialized view mv_player_match_facts.
     * Only useful and called in dev profile.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void run() {
        log.info("[DEV] Running ELO recalculation at startup");
        kickerMatchService.recalculateElo();
        jdbcTemplate.execute("REFRESH MATERIALIZED VIEW mv_player_match_facts");
        log.info("[DEV] ELO recalculation finished");
    }
}