package com.leogouchon.hubscore.user_service.dto;

import com.leogouchon.hubscore.player_service.entity.Players;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class UserResponseDTO {
    private UUID id;
    private String email;
    private Players player;

    public UserResponseDTO(UUID id, String email, Players player) {
        this.id = id;
        this.email = email;
        this.player = player;
    }
}
