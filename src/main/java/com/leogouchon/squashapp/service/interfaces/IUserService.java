package com.leogouchon.squashapp.service.interfaces;

import com.leogouchon.squashapp.model.Users;

import java.util.List;
import java.util.Optional;

public interface IUserService {
    Users createUser(Users users);
    Users getUserByEmail(String username);
    Users getUserByToken(String token);
    Users getUserById(Long id);
    Users updateUser(Users user);
    void deleteUser(Long id);
    Optional<List<Users>> getUsers();
    Optional<List<Users>> getUsersWithLinkedPlayer();
    void updateTokenUser(Users user);
}
