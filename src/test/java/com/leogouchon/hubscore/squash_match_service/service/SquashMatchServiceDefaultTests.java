package com.leogouchon.hubscore.squash_match_service.service;

import com.leogouchon.hubscore.squash_match_service.dto.BatchSessionResponseDTO;
import com.leogouchon.hubscore.squash_match_service.repository.SquashMatchRepository;
import com.leogouchon.hubscore.squash_match_service.repository.projection.SessionMatchProjection;
import com.leogouchon.hubscore.squash_match_service.service.impl.SquashMatchServiceDefault;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SquashMatchServiceDefaultTests {

    @Test
    void should_include_stats_against_each_player_and_sort_tied_players_by_direct_wins_then_points() {
        SquashMatchRepository repository = mock(SquashMatchRepository.class);
        SquashMatchServiceDefault service = new SquashMatchServiceDefault(repository, null);

        long dayUnix = 1_710_000_000L;
        UUID aliceId = UUID.randomUUID();
        UUID bobId = UUID.randomUUID();
        UUID chloeId = UUID.randomUUID();

        when(repository.getSessionDays(any(Pageable.class))).thenReturn(List.of(dayUnix));
        when(repository.countSessionDays()).thenReturn(1L);
        when(repository.getSessionMatches(eq(List.of(dayUnix)))).thenReturn(List.of(
                sessionMatch(dayUnix, aliceId, "Alice", bobId, "Bob", 11, 8),
                sessionMatch(dayUnix, bobId, "Bob", chloeId, "Chloe", 11, 7),
                sessionMatch(dayUnix, aliceId, "Alice", chloeId, "Chloe", 11, 9)
        ));

        Page<BatchSessionResponseDTO> response = service.getMatchesSessionsQuickStats(0, 10);

        assertThat(response.getContent()).hasSize(1);
        BatchSessionResponseDTO session = response.getContent().getFirst();

        assertThat(session.getMatchCount()).isEqualTo(3);
        assertThat(session.getRank()).extracting(rank -> rank.getPlayer().getFirstname())
                .containsExactly("Alice", "Bob", "Chloe");

        assertThat(session.getRank()[0].getWins()).isEqualTo(2);
        assertThat(session.getRank()[0].getStatsAgainstPlayers()).hasSize(2);
        assertThat(session.getRank()[0].getStatsAgainstPlayers())
                .filteredOn(stats -> stats.getOpponent().getFirstname().equals("Bob"))
                .singleElement()
                .satisfies(stats -> {
                    assertThat(stats.getWins()).isEqualTo(1);
                    assertThat(stats.getLosses()).isZero();
                    assertThat(stats.getPointsScored()).isEqualTo(11);
                    assertThat(stats.getPointsReceived()).isEqualTo(8);
                });

        assertThat(session.getRank()[1].getWins()).isEqualTo(1);
        assertThat(session.getRank()[2].getWins()).isEqualTo(0);
    }

    @Test
    void should_use_total_points_when_victory_rate_and_direct_wins_are_tied() {
        SquashMatchRepository repository = mock(SquashMatchRepository.class);
        SquashMatchServiceDefault service = new SquashMatchServiceDefault(repository, null);

        long dayUnix = 1_720_000_000L;
        UUID aliceId = UUID.randomUUID();
        UUID bobId = UUID.randomUUID();
        UUID chloeId = UUID.randomUUID();
        UUID daveId = UUID.randomUUID();

        when(repository.getSessionDays(any(Pageable.class))).thenReturn(List.of(dayUnix));
        when(repository.countSessionDays()).thenReturn(1L);
        when(repository.getSessionMatches(eq(List.of(dayUnix)))).thenReturn(List.of(
                sessionMatch(dayUnix, bobId, "Bob", chloeId, "Chloe", 11, 9),
                sessionMatch(dayUnix, chloeId, "Chloe", bobId, "Bob", 11, 10),
                sessionMatch(dayUnix, bobId, "Bob", daveId, "Dave", 11, 5),
                sessionMatch(dayUnix, daveId, "Dave", bobId, "Bob", 11, 10),
                sessionMatch(dayUnix, chloeId, "Chloe", daveId, "Dave", 11, 7),
                sessionMatch(dayUnix, daveId, "Dave", chloeId, "Chloe", 11, 10),
                sessionMatch(dayUnix, aliceId, "Alice", daveId, "Dave", 11, 8)
        ));

        Page<BatchSessionResponseDTO> response = service.getMatchesSessionsQuickStats(0, 10);

        assertThat(response.getContent()).hasSize(1);
        BatchSessionResponseDTO session = response.getContent().getFirst();

        assertThat(session.getRank()).extracting(rank -> rank.getPlayer().getFirstname())
                .containsSubsequence("Bob", "Chloe");
    }

    private SessionMatchProjection sessionMatch(long dayUnix,
                                                UUID playerAId,
                                                String playerAFirstname,
                                                UUID playerBId,
                                                String playerBFirstname,
                                                int finalScoreA,
                                                int finalScoreB) {
        return new SessionMatchProjection() {
            @Override
            public long getDayUnix() {
                return dayUnix;
            }

            @Override
            public UUID getPlayerAId() {
                return playerAId;
            }

            @Override
            public String getPlayerAFirstname() {
                return playerAFirstname;
            }

            @Override
            public String getPlayerALastname() {
                return null;
            }

            @Override
            public UUID getPlayerBId() {
                return playerBId;
            }

            @Override
            public String getPlayerBFirstname() {
                return playerBFirstname;
            }

            @Override
            public String getPlayerBLastname() {
                return null;
            }

            @Override
            public int getFinalScoreA() {
                return finalScoreA;
            }

            @Override
            public int getFinalScoreB() {
                return finalScoreB;
            }
        };
    }
}
