package com.leogouchon.hubscore.player_service.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Setter
@Getter
@Entity
@Table(name = "players")
public class Players {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "UUID", updatable = false, nullable = false)
    private UUID id;

    private String firstname;
    private String lastname;

    @Column(name = "kicker_current_elo")
    private Integer kickerElo;

    @OneToMany(mappedBy = "player")
    @JsonManagedReference
    private List<PlayerTeam> playerTeams;

    public Players() {}

    public Players(String firstname, String lastname) {
        this.firstname = Objects.requireNonNull(firstname, "Firstname must not be null");
        this.lastname = Objects.requireNonNull(lastname, "Lastname must not be null");
    }

    public Players(UUID id, String firstname, String lastname) {
        this.id = id;
        this.firstname = Objects.requireNonNull(firstname, "Firstname must not be null");
        this.lastname = Objects.requireNonNull(lastname, "Lastname must not be null");
    }

}
