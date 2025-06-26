package com.leogouchon.hubscore.user_service.utils;

import com.leogouchon.hubscore.user_service.dto.MeResponseDTO;
import com.leogouchon.hubscore.user_service.dto.UserResponseDTO;
import com.leogouchon.hubscore.user_service.entity.Users;

public class UsersMapper {
    private UsersMapper() {}

    public static UserResponseDTO toUserResponseDTO(Users user) {
        return new UserResponseDTO(user.getId(), user.getEmail(), user.getPlayer());
    }

    public static MeResponseDTO toMeResponseDTO(Users user) {
        return new MeResponseDTO(user.getId(), user.getEmail(), user.getPlayer(), user.getIsAdmin());
    }
}
