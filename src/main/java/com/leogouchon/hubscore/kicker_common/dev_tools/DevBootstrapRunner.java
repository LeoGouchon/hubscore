package com.leogouchon.hubscore.kicker_common.dev_tools;

import com.leogouchon.hubscore.kicker_match_service.service.KickerMatchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
@DependsOn("flyway")
@Slf4j
public class DevBootstrapRunner {

    private final KickerMatchService kickerMatchService;
    public DevBootstrapRunner(
            KickerMatchService kickerMatchService
    ) {
        this.kickerMatchService = kickerMatchService;
    }

    /**
     * Triggered by an ApplicationReadyEvent, this method runs ELO recalculation.
     * Only useful and called in dev profile.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void run() {
        log.info("[DEV] Running ELO recalculation at startup");
        kickerMatchService.recalculateElo();
        log.info("[DEV] ELO recalculation finished");
    }
}
