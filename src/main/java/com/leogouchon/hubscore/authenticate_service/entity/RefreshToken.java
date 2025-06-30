package com.leogouchon.hubscore.authenticate_service.entity;

import com.leogouchon.hubscore.user_service.entity.Users;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
public class RefreshToken {
    @Id
    @GeneratedValue
    @Column(columnDefinition = "UUID", updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String token;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "users_id", nullable = false)
    private Users user;

    @Column(nullable = false, name = "expiry_date")
    private LocalDateTime expiryDate;

    private boolean revoked;
}
