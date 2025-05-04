package com.leogouchon.squashapp.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
@Entity
public class Users {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false)
    private String email;
    @Column(nullable = false)
    private String password;
    @JsonIgnore
    private Boolean isAdmin = false;
    @OneToOne
    @JoinColumn(name = "players_id", unique = true)
    private Players player;

    @Override
    public String toString() {
        return "Users{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", isAdmin=" + isAdmin +
                ", player=" + (player != null ? player.getId() : "null") +
                '}';
    }

    @Deprecated
    protected Users() {}

    public Users(String email, String password) {
        this.email = Objects.requireNonNull(email, "Email must not be null");
        this.password = Objects.requireNonNull(password, "Password must not be null");
    }

    public Users(String email, String password, Players player) {
        this.email = Objects.requireNonNull(email, "Email must not be null");
        this.password = Objects.requireNonNull(password, "Password must not be null");
        this.player = player;
    }

}
