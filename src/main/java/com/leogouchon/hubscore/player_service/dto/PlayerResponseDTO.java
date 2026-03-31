package com.leogouchon.hubscore.player_service.dto;

import com.leogouchon.hubscore.player_service.entity.Players;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class PlayerResponseDTO {
    private UUID id;
    private String firstname;
    private String lastname;

    public PlayerResponseDTO(Players player) {
        this.id = player.getId();
        this.firstname = player.getFirstname();
        this.lastname = player.getLastname();
    }
}
