package com.leogouchon.hubscore.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DoubleTokenDTO {
    private String accessToken;
    private String refreshToken;
    public DoubleTokenDTO(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }
}
