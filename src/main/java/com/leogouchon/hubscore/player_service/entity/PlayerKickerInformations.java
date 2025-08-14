package com.leogouchon.hubscore.player_service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "player_kicker_informations")
public class PlayerKickerInformations {

    @Id
    @Column(name = "player_id")
    private UUID playerId;

    @Version
    @Column(name = "version")
    private Integer version;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "player_id")
    private Players players;

    @Column(name = "player_current_elo")
    private Integer playerCurrentElo;

    @Column(name = "player_current_seasonal_elo")
    private Integer playerCurrentSeasonalElo;

    public PlayerKickerInformations() {}

    public PlayerKickerInformations(Players players) {
        this.players = players;
    }
}
