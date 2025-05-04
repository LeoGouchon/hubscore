package com.leogouchon.squashapp.dto;

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
