package com.leogouchon.hubscore.user_service.utils;

import com.leogouchon.hubscore.authenticate_service.dto.AuthenticateRequestDTO;
import com.leogouchon.hubscore.user_service.dto.MeResponseDTO;
import com.leogouchon.hubscore.authenticate_service.dto.SignInRequestDTO;
import com.leogouchon.hubscore.user_service.dto.UserResponseDTO;
import com.leogouchon.hubscore.user_service.entity.Users;

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
