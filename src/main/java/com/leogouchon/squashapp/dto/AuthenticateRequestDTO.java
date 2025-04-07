package com.leogouchon.squashapp.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthenticateRequestDTO {
    private String email;
    private String password;
    public AuthenticateRequestDTO(String email, String password) {
        this.email = email;
        this.password = password;
    }
}

