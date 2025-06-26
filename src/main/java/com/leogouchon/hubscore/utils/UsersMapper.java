package com.leogouchon.hubscore.utils;

import com.leogouchon.hubscore.dto.AuthenticateRequestDTO;
import com.leogouchon.hubscore.dto.MeResponseDTO;
import com.leogouchon.hubscore.dto.SignInRequestDTO;
import com.leogouchon.hubscore.dto.UserResponseDTO;
import com.leogouchon.hubscore.model.Users;

public class UsersMapper {
    private UsersMapper() {}

    public static AuthenticateRequestDTO toAuthenticateRequestDTO(Users user) {
        return new AuthenticateRequestDTO(user.getEmail(), user.getPassword());
    }

    public static UserResponseDTO toUserResponseDTO(Users user) {
        return new UserResponseDTO(user.getId(), user.getEmail(), user.getPlayer());
    }

    public static SignInRequestDTO toSignInRequestDTO(Users user) {
        return new SignInRequestDTO(user.getEmail(), user.getPassword());
    }

    public static MeResponseDTO toMeResponseDTO(Users user) {
        return new MeResponseDTO(user.getId(), user.getEmail(), user.getPlayer(), user.getIsAdmin());
    }
}
