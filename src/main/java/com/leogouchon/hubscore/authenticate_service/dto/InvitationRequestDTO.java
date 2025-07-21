package com.leogouchon.hubscore.authenticate_service.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InvitationRequestDTO {
    private String playerId;

    public InvitationRequestDTO(String playerId) {
        this.playerId = playerId;
    }
}
