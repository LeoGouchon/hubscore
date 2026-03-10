package com.leogouchon.hubscore.squash_match_service.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.leogouchon.hubscore.player_service.entity.Players;
import com.leogouchon.hubscore.common.type.MatchPoint;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Setter
@Getter
@Entity
@Table(name = "squash_matches")
public class SquashMatches {
    @Id
    @GeneratedValue
    @Column(columnDefinition = "UUID", updatable = false, nullable = false)
    private UUID id;

    @OneToMany(mappedBy = "squashMatch", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JsonIgnore
    private List<SquashPoints> points = new ArrayList<>();

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

    protected SquashMatches() {}

    public SquashMatches(Players playerA, Players playerB) {
        if (playerA.equals(playerB)) {
            throw new IllegalArgumentException("Players must be different");
        }
        this.playerA = playerA;
        this.playerB = playerB;
        this.startTime = new Timestamp(System.currentTimeMillis());
    }

    public SquashMatches(Players playerA, Players playerB, Integer finalScoreA, Integer finalScoreB) {
        this(playerA, playerB);
        this.finalScoreA = finalScoreA;
        this.finalScoreB = finalScoreB;
        if (Boolean.FALSE.equals(this.isFinished())) {
            throw new IllegalArgumentException("Match must be finished to create it");
        }
    }

    @Transient
    public List<MatchPoint> getPointsHistory() {
        return points.stream()
                .sorted(Comparator.comparing(SquashPoints::getPointOrder))
                .map(SquashPoints::toMatchPoint)
                .toList();
    }

    public void setPointsHistory(List<MatchPoint> pointsHistory) {
        this.points.clear();
        if (pointsHistory == null) {
            return;
        }

        for (int i = 0; i < pointsHistory.size(); i++) {
            this.points.add(new SquashPoints(this, i, pointsHistory.get(i)));
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
