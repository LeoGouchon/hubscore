package com.leogouchon.hubscore.kicker_match_service.entity;

import com.leogouchon.hubscore.player_service.entity.Players;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Setter
@Getter
@Entity
@Table(name = "kicker_elo")
public class KickerElo {

    @EmbeddedId
    private KickerEloId id;

    @ManyToOne
    @MapsId("matchId")
    @JoinColumn(name = "match_id", referencedColumnName = "id")
    private KickerMatches match;

    @ManyToOne
    @MapsId("playerId")
    @JoinColumn(name = "player_id", referencedColumnName = "id")
    private Players player;

    @Column(name= "elo_after_match")
    private Integer eloAfterMatch;

    @Column(name= "elo_before_match")
    private Integer eloBeforeMatch;

    @Column(name= "elo_change")
    private Integer eloChange;

    @Column(name= "created_at")
    private Timestamp createdAt;

    public KickerElo() {}
}
