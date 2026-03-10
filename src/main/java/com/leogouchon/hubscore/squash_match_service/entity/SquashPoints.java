package com.leogouchon.hubscore.squash_match_service.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.leogouchon.hubscore.common.enums.PlayerLetter;
import com.leogouchon.hubscore.common.enums.ServiceSide;
import com.leogouchon.hubscore.common.type.MatchPoint;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.UUID;

@Setter
@Getter
@Entity
@Table(name = "squash_points")
public class SquashPoints {
    @Id
    @GeneratedValue
    @Column(columnDefinition = "UUID", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "squash_match_id", referencedColumnName = "id")
    @JsonIgnore
    private SquashMatches squashMatch;

    @Column(name = "point_order", nullable = false)
    private Integer pointOrder;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "server", nullable = false)
    private PlayerLetter server;

    @Enumerated(EnumType.STRING)
    @Column(name = "service_side", nullable = false)
    private ServiceSide serviceSide;

    @Enumerated(EnumType.STRING)
    @Column(name = "scorer", nullable = false)
    private PlayerLetter scorer;

    @Column(name = "score_a", nullable = false)
    private Integer scoreA;

    @Column(name = "score_b", nullable = false)
    private Integer scoreB;

    protected SquashPoints() {}

    public SquashPoints(SquashMatches squashMatch, Integer pointOrder, MatchPoint point) {
        this.squashMatch = squashMatch;
        this.pointOrder = pointOrder;
        this.createdAt = point.getCreatedAt();
        this.server = point.getServer();
        this.serviceSide = point.getServiceSide();
        this.scorer = point.getScorer();
        this.scoreA = point.getScoreA();
        this.scoreB = point.getScoreB();
    }

    public MatchPoint toMatchPoint() {
        return new MatchPoint(server, serviceSide, scorer, scoreA, scoreB, createdAt);
    }
}
