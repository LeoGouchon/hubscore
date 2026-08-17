package com.leogouchon.husbcore.user_service.dto;

import com.leogouchon.husbcore.player_service.entity.Players;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class MeResponseDTO {
    private UUID id;
    private String email;
    private Players player;
    private String role;

    public MeResponseDTO(UUID id, String email, Players player, String role) {
        this.id = id;
        this.email = email;
        this.player = player;
        this.role = role;
    }
}
