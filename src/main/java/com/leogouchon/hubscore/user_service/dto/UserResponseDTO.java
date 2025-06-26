package com.leogouchon.hubscore.user_service.dto;

import com.leogouchon.hubscore.player_service.entity.Players;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserResponseDTO {
    private Long id;
    private String email;
    private Players player;

    public UserResponseDTO(Long id, String email, Players player) {
        this.id = id;
        this.email = email;
        this.player = player;
    }
}
