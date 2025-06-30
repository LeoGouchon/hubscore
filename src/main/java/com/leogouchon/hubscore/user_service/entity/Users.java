package com.leogouchon.hubscore.user_service.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.leogouchon.hubscore.authenticate_service.entity.RefreshToken;
import com.leogouchon.hubscore.player_service.entity.Players;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
@Entity
public class Users {
    @Id
    @GeneratedValue
    @Column(columnDefinition = "UUID", updatable = false, nullable = false)
    private UUID id;
    @Column(unique = true, nullable = false)
    private String email;
    @Column(nullable = false)
    private String password;
    @JsonIgnore
    @Column(name = "is_admin")
    private Boolean isAdmin = false;
    @OneToOne
    @JoinColumn(name = "players_id", unique = true)
    private Players player;
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RefreshToken> refreshTokens = new ArrayList<>();

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
