package com.leogouchon.hubscore.kicker_match_service.service;

import com.leogouchon.hubscore.kicker_match_service.service.impl.EloCalculatorDefault;
import com.leogouchon.hubscore.player_service.entity.Players;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class EloCalculatorDefaultTest {

    private EloCalculatorDefault eloCalculator;

    private Players player1;
    private Players player2;

    private UUID player1Id;
    private UUID player2Id;

    @BeforeEach
    void setUp() {
        eloCalculator = new EloCalculatorDefault();

        player1Id = UUID.randomUUID();
        player2Id = UUID.randomUUID();

        player1 = new Players();
        player1.setId(player1Id);

        player2 = new Players();
        player2.setId(player2Id);
    }

    // --------------------
    // getInitialElo
    // --------------------

    @Test
    void should_return_1500_as_initial_elo() {
        assertThat(eloCalculator.getInitialELo()).isEqualTo(1500);
    }

    // --------------------
    // calculateDeltaElo
    // --------------------

    @Test
    void should_calculate_positive_delta_elo() {
        int delta = eloCalculator.calculateDeltaElo(20, 1.0, 0.75);

        assertThat(delta).isEqualTo(5);
    }

    @Test
    void should_calculate_negative_delta_elo() {
        int delta = eloCalculator.calculateDeltaElo(20, 0.0, 0.5);

        assertThat(delta).isEqualTo(-10);
    }

    // --------------------
    // averageElo
    // --------------------

    @Test
    void should_return_average_elo_for_two_players() {
        Map<UUID, Integer> elos = Map.of(
                player1Id, 1600,
                player2Id, 1400
        );

        double avg = eloCalculator.averageElo(player1, player2, elos);

        assertThat(avg).isEqualTo(1500.0);
    }

    @Test
    void should_return_single_player_elo_when_second_player_is_null() {
        Map<UUID, Integer> elos = Map.of(player1Id, 1550);

        double avg = eloCalculator.averageElo(player1, null, elos);

        assertThat(avg).isEqualTo(1550.0);
    }

    @Test
    void should_throw_when_player1_elo_missing() {
        Map<UUID, Integer> elos = Map.of();

        assertThatThrownBy(() ->
                eloCalculator.averageElo(player1, null, elos)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(player1Id.toString());
    }

    @Test
    void should_throw_when_player2_elo_missing() {
        Map<UUID, Integer> elos = Map.of(
                player1Id, 1500
        );

        assertThatThrownBy(() ->
                eloCalculator.averageElo(player1, player2, elos)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(player2Id.toString());
    }

    // --------------------
    // calculateK
    // --------------------

    @Test
    void should_clamp_score_diff_to_minimum_of_1() {
        int k = eloCalculator.calculateK(0);

        assertThat(k).isGreaterThan(0);
    }

    @Test
    void should_clamp_score_diff_to_maximum() {
        int kAtMax = eloCalculator.calculateK(20);
        int kAboveMax = eloCalculator.calculateK(100);

        assertThat(kAboveMax).isEqualTo(kAtMax);
    }

    @Test
    void should_increase_k_when_score_diff_increases() {
        int kSmall = eloCalculator.calculateK(2);
        int kBig = eloCalculator.calculateK(10);

        assertThat(kBig).isGreaterThan(kSmall);
    }

    // --------------------
    // getScore
    // --------------------

    @Test
    void should_return_0_5_when_draw() {
        double score = eloCalculator.getScore(5, 5);

        assertThat(score).isEqualTo(0.5);
    }

    @Test
    void should_return_score_greater_than_0_5_for_winner() {
        double score = eloCalculator.getScore(10, 5);

        assertThat(score)
                .isGreaterThan(0.5)
                .isLessThanOrEqualTo(1.0);
    }

    @Test
    void should_return_score_lower_than_0_5_for_loser() {
        double score = eloCalculator.getScore(5, 10);

        assertThat(score)
                .isLessThan(0.5)
                .isGreaterThanOrEqualTo(0.0);
    }

    // --------------------
    // exceptedResult
    // --------------------

    @Test
    void should_return_0_5_when_elos_are_equal() {
        double result = eloCalculator.expectedResult(1500, 1500);

        assertThat(result).isEqualTo(0.5);
    }

    @Test
    void should_favor_higher_elo_team() {
        double result = eloCalculator.expectedResult(1600, 1400);

        assertThat(result).isGreaterThan(0.5);
    }

    @Test
    void should_disfavor_lower_elo_team() {
        double result = eloCalculator.expectedResult(1400, 1600);

        assertThat(result).isLessThan(0.5);
    }
}
