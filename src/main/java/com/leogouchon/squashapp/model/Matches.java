package com.leogouchon.squashapp.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.leogouchon.squashapp.model.types.MatchPoint;
import com.leogouchon.squashapp.utils.PointListConverter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.List;

@Setter
@Getter
@Entity
@Table(name = "matches")
public class Matches {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Convert(converter = PointListConverter.class)
    @Column(columnDefinition = "TEXT", name = "points_history")
    private List<MatchPoint> pointsHistory;

    @Column(name = "final_score_a")
    private Integer finalScoreA;
    @Column(name = "final_score_b")
    private Integer finalScoreB;
    @Column(name = "start_time")
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private Timestamp startTime;
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    @Column(name = "end_time")
    private Timestamp endTime;

    @ManyToOne
    @JoinColumn(name = "player_a_id", referencedColumnName = "id")
    private Players playerA;

    @ManyToOne
    @JoinColumn(name = "player_b_id", referencedColumnName = "id")
    private Players playerB;

    protected Matches() {}

    public Matches(Players playerA, Players playerB) {
        if (playerA.equals(playerB)) {
            throw new IllegalArgumentException("Players must be different");
        }
        this.playerA = playerA;
        this.playerB = playerB;
        this.startTime = new Timestamp(System.currentTimeMillis());
    }

    public Matches(Players playerA, Players playerB, Integer finalScoreA, Integer finalScoreB) {
        this(playerA, playerB);
        this.finalScoreA = finalScoreA;
        this.finalScoreB = finalScoreB;
        if (Boolean.FALSE.equals(this.isFinished())) {
            throw new IllegalArgumentException("Match must be finished to create it");
        }
    }

    public Matches(Players playerA, Players playerB, List<MatchPoint> pointsHistory, Integer finalScoreA, Integer finalScoreB) {
        this(playerA, playerB);
        this.finalScoreA = finalScoreA;
        this.finalScoreB = finalScoreB;
        this.pointsHistory = pointsHistory;
        if (Boolean.FALSE.equals(this.isFinished())) {
            throw new IllegalArgumentException("Match must be finished to create it");
        }
    }

    /**
     * Determines if the match is finished.
     * <p>
     * A match is finished when the absolute difference between the two players
     * is 2 or more and at least one of the players has scored 11 points.
     * If the match has already been finished, this method will return true as well.
     *
     * @return true if the match is finished, false otherwise
     */
    public Boolean isFinished() {
        if (Math.abs(finalScoreA - finalScoreB) >= 2 && (finalScoreA >= 11 || finalScoreB >= 11)) {
            if (this.endTime == null) {
                this.endTime = new Timestamp(System.currentTimeMillis());
            }
            return true;
        }
        return false;
    }
}
