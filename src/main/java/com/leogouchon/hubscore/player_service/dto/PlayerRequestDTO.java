package com.leogouchon.hubscore.player_service.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class PlayerRequestDTO {
    private String firstname;
    private String lastname;
    private List<UUID> teamIds;

    public PlayerRequestDTO(String firstname, String lastname, List<UUID> teamIds) {
        this.firstname = firstname;
        this.lastname = lastname;
        this.teamIds = teamIds;
    }
}
