package com.leogouchon.hubscore.player_service.dto;

import com.leogouchon.hubscore.player_service.entity.Teams;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class TeamResponseDTO {
    private UUID id;
    private String name;
    private String shortname;
    private boolean isKicker;
    private boolean isSquash;

    public TeamResponseDTO(Teams team) {
        this.id = team.getId();
        this.name = team.getName();
        this.shortname = team.getShortname();
        this.isKicker = team.isKicker();
        this.isSquash = team.isSquash();
    }
}
