package com.leogouchon.hubscore.user_service.dto;

import com.leogouchon.hubscore.player_service.entity.Players;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class MeResponseDTO {
    private UUID id;
    private String email;
    private Players player;
    private boolean isAdmin;

    public MeResponseDTO(UUID id, String email, Players player, boolean isAdmin) {
        this.id = id;
        this.email = email;
        this.player = player;
        this.isAdmin = isAdmin;
    }
}
