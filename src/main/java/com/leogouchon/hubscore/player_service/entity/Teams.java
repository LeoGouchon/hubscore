package com.leogouchon.hubscore.player_service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
@Entity
@Table(name = "teams")
public class Teams {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "UUID", updatable = false, nullable = false)
    private UUID id;

    private String name;
    private String shortname;

    @Column(name = "is_kicker")
    private boolean isKicker;
    @Column(name = "is_squash")
    private boolean isSquash;

    public Teams() {}
}
