package com.leogouchon.hubscore.authenticate_service.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthenticateResponseDTO {
    private String token;

    public AuthenticateResponseDTO(String token) {
        this.token = token;
    }
}
