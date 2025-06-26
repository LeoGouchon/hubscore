package com.leogouchon.hubscore.user_service.dto;

import com.leogouchon.hubscore.player_service.entity.Players;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MeResponseDTO {
    private Long id;
    private String email;
    private Players player;
    private boolean isAdmin;

    public MeResponseDTO(Long id, String email, Players player, boolean isAdmin) {
        this.id = id;
        this.email = email;
        this.player = player;
        this.isAdmin = isAdmin;
    }
}
