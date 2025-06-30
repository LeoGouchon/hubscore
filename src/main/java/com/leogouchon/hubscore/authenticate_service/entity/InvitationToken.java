package com.leogouchon.hubscore.authenticate_service.entity;

import com.leogouchon.hubscore.player_service.entity.Players;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
public class InvitationToken {
    @Id
    @GeneratedValue
    @Column(columnDefinition = "UUID", updatable = false, nullable = false)
    private UUID id;
    private String token;

    @ManyToOne
    @JoinColumn(name = "player_id", referencedColumnName = "id")
    private Players player;
    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;
    @Column(name = "is_used")
    private Boolean isUsed = false;

    public InvitationToken() {}

    public InvitationToken(String token, Players player, LocalDateTime expiryDate) {
        this.token = token;
        this.player = player;
        this.expiryDate = expiryDate;
    }
}