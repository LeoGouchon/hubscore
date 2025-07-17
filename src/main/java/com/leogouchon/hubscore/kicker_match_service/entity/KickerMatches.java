package com.leogouchon.hubscore.kicker_match_service.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.leogouchon.hubscore.player_service.entity.Players;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Setter
@Getter
@Entity
@Table(name = "kicker_matches")
public class KickerMatches {
    @Id
    @GeneratedValue
    @Column(columnDefinition = "UUID", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "final_score_team_a")
    private Integer scoreA;
    @Column(name = "final_score_team_b")
    private Integer scoreB;

    @Column(name = "created_at")
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private Timestamp createdAt;

    @ManyToOne
    @JoinColumn(name = "player_one_team_a_id", referencedColumnName = "id")
    private Players player1A;

    @ManyToOne
    @JoinColumn(name = "player_two_team_a_id", referencedColumnName = "id", nullable = true)
    private Players player2A;

    @ManyToOne
    @JoinColumn(name = "player_one_team_b_id", referencedColumnName = "id")
    private Players player1B;

    @ManyToOne
    @JoinColumn(name = "player_two_team_b_id", referencedColumnName = "id", nullable = true)
    private Players player2B;

    protected KickerMatches() {}

    public KickerMatches(Players player1TeamA, Players player2TeamA, Players player1TeamB, Players player2TeamB) {
        if (player1TeamA == null || player1TeamB == null) {
            throw new IllegalArgumentException("Each team must have at least one player");
        }

        boolean is1v1 = player2TeamA == null && player2TeamB == null;
        boolean is2v2 = player2TeamA != null && player2TeamB != null;

        if (!is1v1 && !is2v2) {
            throw new IllegalArgumentException("Must have either two or four players (1v1 or 2v2)");
        }

        Set<Players> players = new HashSet<>();
        players.add(player1TeamA);
        players.add(player1TeamB);
        if (player2TeamA != null) players.add(player2TeamA);
        if (player2TeamB != null) players.add(player2TeamB);

        int expectedCount = is1v1 ? 2 : 4;
        if (players.size() != expectedCount) {
            throw new IllegalArgumentException("Players must be distinct");
        }

        this.player1A = player1TeamA;
        this.player2A = player2TeamA;
        this.player1B = player1TeamB;
        this.player2B = player2TeamB;
        this.createdAt = new Timestamp(System.currentTimeMillis());
    }

    public KickerMatches(Players player1TeamA, Players player2TeamA, Players player1TeamB,
                         Players player2TeamB, Integer finalScoreTeamA, Integer finalScoreTeamB) {
        this(player1TeamA, player2TeamA, player1TeamB, player2TeamB);

        if (finalScoreTeamA == null || finalScoreTeamB == null) {
            throw new IllegalArgumentException("Scores must not be null");
        }

        if (!isFinished(finalScoreTeamA, finalScoreTeamB)) {
            throw new IllegalArgumentException("Match must be finished to create it");
        }

        this.scoreA = finalScoreTeamA;
        this.scoreB = finalScoreTeamB;
    }

    public Boolean isFinished(Integer scoreA, Integer scoreB) {
        return scoreA != null && scoreB != null && (scoreA == 10 || scoreB == 10);
    }
}
