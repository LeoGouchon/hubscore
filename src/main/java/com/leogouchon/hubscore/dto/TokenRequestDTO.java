package com.leogouchon.hubscore.dto;

import lombok.Getter;

@Getter
public class TokenRequestDTO {
    private final String accessToken;

    public TokenRequestDTO(String accessToken) {
        this.accessToken = accessToken;
    }
}
