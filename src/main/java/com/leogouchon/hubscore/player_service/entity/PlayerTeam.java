package com.leogouchon.hubscore.player_service.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "player_team")
public class PlayerTeam {
    @EmbeddedId
    private PlayerTeamId id;

    @ManyToOne
    @MapsId("playerId")
    @JsonBackReference
    private Players player;

    @ManyToOne
    @MapsId("teamId")
    private Teams team;
}
