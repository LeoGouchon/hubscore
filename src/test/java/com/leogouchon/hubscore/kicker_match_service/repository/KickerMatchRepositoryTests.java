package com.leogouchon.hubscore.kicker_match_service.repository;

import com.leogouchon.hubscore.kicker_match_service.repository.projection.GlobalStatsResponseProjection;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

// Ignore a local ./config/application.yml (dev profile, seed data) so the test runs as in CI.
@DataJpaTest(properties = "spring.config.location=classpath:/")
@AutoConfigureTestDatabase(replace = Replace.NONE)
@ActiveProfiles({"test", "postgres"})
@Testcontainers(disabledWithoutDocker = true)
class KickerMatchRepositoryTests {
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 11, 25, 12, 0);
    private static final LocalDateTime ACTIVE_SINCE = NOW.minusDays(90);
    private static final int MIN_RANKED_MATCHES = 10;

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17");

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private KickerMatchRepository repository;

    @Test
    void should_apply_flyway_migrations_when_postgres_starts() {
        Integer facts = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM mv_player_match_facts", Integer.class);

        assertThat(facts).isZero();
    }

    @Test
    void should_rank_only_active_players_with_enough_matches() {
        BriefCases cases = insertBriefCases();
        refreshFacts();

        Map<UUID, GlobalStatsResponseProjection> stats = globalStats();

        assertThat(stats.get(cases.active()).getRank()).isEqualTo(1);
        assertThat(stats.get(cases.returning()).getRank()).isEqualTo(2);
        assertThat(stats.get(cases.justRanked()).getRank()).isEqualTo(3);
        assertThat(stats.get(cases.inactive()).getRank()).isZero();
        assertThat(stats.get(cases.inactive()).getCurrentElo()).isEqualTo(1700);
        assertThat(stats.get(cases.newbie()).getRank()).isZero();
        assertThat(stats.get(cases.newbie()).getCurrentElo()).isNull();
    }

    @Test
    void should_rank_player_when_last_match_is_exactly_at_active_since() {
        UUID player = insertPlayer("Edge", 1500);
        insertMatches(player, 10, ACTIVE_SINCE, 1500);
        refreshFacts();

        Map<UUID, GlobalStatsResponseProjection> stats = globalStats();

        assertThat(stats.get(player).getRank()).isEqualTo(1);
    }

    @Test
    void should_return_all_players_unranked_when_nobody_is_active() {
        UUID first = insertPlayer("First", 1600);
        insertMatches(first, 10, NOW.minusDays(100), 1600);
        UUID second = insertPlayer("Second", 1500);
        insertMatches(second, 10, NOW.minusDays(100), 1500);
        refreshFacts();

        Map<UUID, GlobalStatsResponseProjection> stats = globalStats();

        assertThat(stats).containsOnlyKeys(first, second);
        assertThat(stats.values()).allSatisfy(stat -> {
            assertThat(stat.getRank()).isZero();
            assertThat(stat.getCurrentElo()).isNotNull();
        });
    }

    private Map<UUID, GlobalStatsResponseProjection> globalStats() {
        List<GlobalStatsResponseProjection> stats =
                repository.getGlobalKickerStats(null, null, MIN_RANKED_MATCHES, ACTIVE_SINCE);
        return stats.stream()
                .collect(Collectors.toMap(GlobalStatsResponseProjection::getPlayerId, Function.identity()));
    }

    private BriefCases insertBriefCases() {
        UUID active = insertPlayer("Active", 1600);
        insertMatches(active, 11, NOW.minusDays(3), 1590);

        UUID inactive = insertPlayer("Inactive", 1700);
        insertMatches(inactive, 10, NOW.minusDays(120), 1700);

        UUID returning = insertPlayer("Returning", 1550);
        insertMatches(returning, 10, NOW.minusDays(180), 1540);
        insertMatch(returning, NOW.minusDays(1), 1550);

        UUID newbie = insertPlayer("Newbie", 1400);
        insertMatches(newbie, 6, NOW.minusDays(1), 1400);

        UUID justRanked = insertPlayer("JustRanked", 1500);
        insertMatches(justRanked, 9, NOW.minusDays(10), 1490);
        insertMatch(justRanked, NOW.minusDays(1), 1500);

        return new BriefCases(active, inactive, returning, newbie, justRanked);
    }

    private UUID insertPlayer(String firstname, int currentElo) {
        UUID id = UUID.randomUUID();
        jdbcTemplate.update("INSERT INTO players (id, firstname, lastname) VALUES (?, ?, ?)", id, firstname, "Test");
        jdbcTemplate.update(
                "INSERT INTO player_kicker_informations (player_id, player_current_elo) VALUES (?, ?)",
                id,
                currentElo
        );
        return id;
    }

    // Only the tracked player gets ELO rows, so the opponent stays out of mv_player_match_facts.
    private void insertMatch(UUID player, LocalDateTime playedAt, int eloAfter) {
        UUID opponent = UUID.randomUUID();
        jdbcTemplate.update("INSERT INTO players (id, firstname, lastname) VALUES (?, ?, ?)", opponent, "Opponent", "Test");

        UUID match = UUID.randomUUID();
        jdbcTemplate.update("""
                INSERT INTO kicker_matches (id, final_score_team_a, final_score_team_b, created_at,
                                            player_one_team_a_id, player_one_team_b_id)
                VALUES (?, 10, 5, ?, ?, ?)
                """, match, playedAt, player, opponent);
        jdbcTemplate.update("""
                INSERT INTO kicker_elo (match_id, player_id, elo_before_match, elo_after_match, elo_change, created_at)
                VALUES (?, ?, ?, ?, 10, ?)
                """, match, player, eloAfter - 10, eloAfter, playedAt);
        jdbcTemplate.update("""
                INSERT INTO kicker_elo_seasonal (match_id, player_id, elo_before_match, elo_after_match, elo_change,
                                                 created_at, season_year, season_quarter)
                VALUES (?, ?, ?, ?, 10, ?, ?, ?)
                """, match, player, eloAfter - 10, eloAfter, playedAt, playedAt.getYear(),
                (playedAt.getMonthValue() - 1) / 3 + 1);
    }

    private void insertMatches(UUID player, int count, LocalDateTime lastPlayedAt, int eloAfterLast) {
        for (int i = count - 1; i >= 0; i--) {
            insertMatch(player, lastPlayedAt.minusDays(i), eloAfterLast);
        }
    }

    private void refreshFacts() {
        jdbcTemplate.execute("REFRESH MATERIALIZED VIEW mv_player_match_facts");
    }

    private record BriefCases(UUID active, UUID inactive, UUID returning, UUID newbie, UUID justRanked) {
    }
}
