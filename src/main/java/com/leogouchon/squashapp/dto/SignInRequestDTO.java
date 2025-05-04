package com.leogouchon.squashapp.dto;

import com.leogouchon.squashapp.model.Players;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignInRequestDTO {
    private String email;
    private String password;
    private Players player;

    public SignInRequestDTO(String email, String password, Players player) {
        this.email = email;
        this.password = password;
        this.player = player;
    }
}
