package com.leogouchon.hubscore.authenticate_service.entity;

import com.leogouchon.hubscore.player_service.entity.Players;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
public class InvitationToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
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