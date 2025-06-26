package com.leogouchon.hubscore.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignInRequestDTO {
    private String email;
    private String password;

    public SignInRequestDTO(String email, String password) {
        this.email = email;
        this.password = password;
    }
}
