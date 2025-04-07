package com.leogouchon.squashapp.service;

import com.leogouchon.squashapp.dto.AuthenticateRequestDTO;
import com.leogouchon.squashapp.model.Users;
import com.leogouchon.squashapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.naming.AuthenticationException;
import java.util.UUID;

@Service
public class AuthenticateService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    @Autowired
    public AuthenticateService(UserService userService, PasswordEncoder passwordEncoder, UserRepository userRepository) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }

    public String login(AuthenticateRequestDTO user) throws AuthenticationException {
        Users existingUsers = userService.getUserByEmail(user.getEmail());
        if (existingUsers == null || !matchPassword(existingUsers, user.getPassword())) {
            throw new AuthenticationException("user or password incorrect");
        }
        String token = generateToken(existingUsers);
        userService.updateTokenUser(existingUsers);
        return token;
    }

    public void logout(String token) {
        Users users = userService.getUserByToken(token);
        if (users != null) {
            users.setToken(null);
            userService.updateTokenUser(users);
        }
    }

    public boolean isValidToken(String token) {
        Users users = userService.getUserByToken(token);
        return users != null && users.getToken().equals(token);
    }

    public boolean isAdmin(String token) {
        Users users = userService.getUserByToken(token);
        return users != null && users.getIsAdmin();
    }

    public String generateToken(Users user) {
        String token = UUID.randomUUID().toString();
        user.setToken(token);
        return userRepository.save(user).getToken();
    }

    public boolean matchPassword(Users existingUsers, String unhashedPassword) {
        return passwordEncoder.matches(unhashedPassword, existingUsers.getPassword());
    }
}
