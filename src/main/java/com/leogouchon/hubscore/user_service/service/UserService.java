package com.leogouchon.hubscore.user_service.service;

import com.leogouchon.hubscore.user_service.entity.Users;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface UserService {
    Users createUser(Users users);
    Users getUserByEmail(String username);
    Users getUserById(UUID id);
    Users updateUser(Users user);
    void deleteUser(UUID id);
    Page<Users> getUsers(int page, int size);
}

