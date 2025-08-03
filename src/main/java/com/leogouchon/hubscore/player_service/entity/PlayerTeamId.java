package com.leogouchon.hubscore.player_service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@Embeddable
public class PlayerTeamId implements Serializable {
    @Column(name = "player_id")
    private UUID playerId;

    @Column(name = "team_id")
    private UUID teamId;
}
