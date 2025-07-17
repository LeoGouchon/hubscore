package com.leogouchon.hubscore.kicker_match_service.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.leogouchon.hubscore.player_service.entity.Players;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.List;
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
    @JoinColumn(name = "player_two_team_a_id", referencedColumnName = "id")
    private Players player2A;

    @ManyToOne
    @JoinColumn(name = "player_one_team_b_id", referencedColumnName = "id")
    private Players player1B;

    @ManyToOne
    @JoinColumn(name = "player_two_team_b_id", referencedColumnName = "id")
    private Players player2B;

    protected KickerMatches() {}

    public KickerMatches(Players player1TeamA, Players player2TeamA, Players player1TeamB, Players player2TeamB) {
        if (player1TeamA == null || player1TeamB == null) {
            throw new IllegalArgumentException("Players must not be null");
        }
        else if (player1TeamA.equals(player1TeamB)) {
            throw new IllegalArgumentException("Players must be different");
        }
        else if ((player2TeamA != null && player2TeamB == null) || (player2TeamA == null && player2TeamB != null)) {
            throw new IllegalArgumentException("Must have exactly two or four different players");
        } else if (new HashSet<>(List.of(player1TeamA, player1TeamB, player2TeamA, player2TeamB)).size() == 4 || new HashSet<>(List.of(player1TeamA, player1TeamB, player2TeamA, player2TeamB)).size() == 2) {
            this.player1A = player1TeamA;
            this.player2A = player2TeamA;
            this.player1B = player1TeamB;
            this.player2B = player2TeamB;
            this.createdAt = new Timestamp(System.currentTimeMillis());
        } else {
            throw new IllegalArgumentException("Must have exactly two or four different players");
        }

    }

    public KickerMatches(Players player1TeamA, Players player2TeamA, Players player1TeamB, Players player2TeamB, Integer finalScoreTeamA, Integer finalScoreTeamB) {
        this(player1TeamA, player2TeamA, player1TeamB, player2TeamB);
        if (Boolean.FALSE.equals(this.isFinished(finalScoreTeamA, finalScoreTeamB))) {
            throw new IllegalArgumentException("Match must be finished to create it");
        }
        this.scoreA = finalScoreTeamA;
        this.scoreB = finalScoreTeamB;
    }


    public Boolean isFinished(Integer scoreA, Integer scoreB) {
        return scoreA == 10 || scoreB == 10;
    }
}
