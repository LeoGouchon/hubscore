package com.leogouchon.hubscore.authenticate_service.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TokenRefreshRequestDTO {
    private String refreshToken;

    public TokenRefreshRequestDTO(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
