package com.leogouchon.hubscore.service.interfaces;

import com.leogouchon.hubscore.model.Users;
import org.springframework.data.domain.Page;

public interface IUserService {
    Users createUser(Users users);
    Users getUserByEmail(String username);
    Users getUserById(Long id);
    Users updateUser(Users user);
    void deleteUser(Long id);
    Page<Users> getUsers(int page, int size);
}
