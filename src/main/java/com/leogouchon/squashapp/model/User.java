package com.leogouchon.squashapp.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false)
    private String email;
    @Column(nullable = false)
    private String password;
    private String token;
    @JsonIgnore
    private Boolean isAdmin = false;
    @OneToOne
    @JoinColumn(name = "players_id", unique = true)
    private Player player;

    @Override
    public String toString() {
        return "Users{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", token='" + token + '\'' +
                ", isAdmin=" + isAdmin +
                ", player=" + (player != null ? player.getId() : "null") +
                '}';
    }

    @Deprecated
    protected User () {}

    public User(String email, String password) {
        this.email = Objects.requireNonNull(email, "Email must not be null");
        this.password = Objects.requireNonNull(password, "Password must not be null");
    }

    public User(String email, String password, Player player) {
        this.email = Objects.requireNonNull(email, "Email must not be null");
        this.password = Objects.requireNonNull(password, "Password must not be null");
        this.player = player;
    }

}
