package com.leogouchon.hubscore.authenticate_service.dto;

import lombok.Getter;

@Getter
public class TokenRequestDTO {
    private final String accessToken;

    public TokenRequestDTO(String accessToken) {
        this.accessToken = accessToken;
    }
}
