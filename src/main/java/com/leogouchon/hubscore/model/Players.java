package com.leogouchon.hubscore.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Setter
@Getter
@Entity
@Table(name = "players")
public class Players {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstname;
    private String lastname;

    public Players() {}

    public Players(String firstname, String lastname) {
        this.firstname = Objects.requireNonNull(firstname, "Firstname must not be null");
        this.lastname = Objects.requireNonNull(lastname, "Lastname must not be null");
    }

}
