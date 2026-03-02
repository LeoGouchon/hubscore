package com.leogouchon.hubscore.kicker_match_service.service.impl;

import com.leogouchon.hubscore.kicker_match_service.service.PlayerMatchFactsViewService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
public class PlayerMatchFactsViewServiceDefault implements PlayerMatchFactsViewService {
    private final JdbcTemplate jdbcTemplate;

    public PlayerMatchFactsViewServiceDefault(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void refresh() {
        jdbcTemplate.execute("REFRESH MATERIALIZED VIEW mv_player_match_facts");
    }

    @Override
    public void refreshAfterCommit() {
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            refresh();
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                refresh();
            }
        });
    }
}
