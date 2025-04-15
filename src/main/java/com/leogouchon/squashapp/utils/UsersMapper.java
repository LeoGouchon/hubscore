package com.leogouchon.squashapp.utils;

import com.leogouchon.squashapp.dto.AuthenticateRequestDTO;
import com.leogouchon.squashapp.model.Users;

public class UsersMapper {
    public static AuthenticateRequestDTO toAuthenticateRequestDTO(Users user) {
        return new AuthenticateRequestDTO(user.getEmail(), user.getPassword());
    }
}
