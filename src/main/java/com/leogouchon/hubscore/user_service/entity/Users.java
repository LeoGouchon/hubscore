package com.leogouchon.husbcore.user_service.entity;

import com.leogouchon.husbcore.player_service.entity.Players;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

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
    @Column(name = "identity_user_id", unique = true)
    private UUID identityUserId;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private UserRole role = UserRole.USER;
    @OneToOne
    @JoinColumn(name = "players_id", unique = true)
    private Players player;

    @Deprecated
    protected Users() {
    }

    public Users(String email) {
        this.email = Objects.requireNonNull(email, "Email must not be null");
    }

    public Users(String email, Players player) {
        this(email);
        this.player = player;
    }

    public UserRole effectiveRole() {
        return role == null ? UserRole.USER : role;
    }

    @Override
    public String toString() {
        return "Users{" + "id=" + id + ", email='" + email + '\'' + ", role=" + role + ", player=" + (player == null ? "null" : player.getId()) + '}';
    }
}
