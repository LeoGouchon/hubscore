package com.leogouchon.squashapp.service.interfaces;

import com.leogouchon.squashapp.model.Users;

import javax.naming.AuthenticationException;
import java.util.List;

public interface IUserService {
    Users createUser(Users users);
    Users getUserByEmail(String username);
    Users getUserByToken(String token) throws AuthenticationException;
    Users getUserById(Long id);
    Users updateUser(Users user);
    void deleteUser(Long id);
    List<Users> getUsers();
    List<Users> getUsersWithLinkedPlayer();
}
