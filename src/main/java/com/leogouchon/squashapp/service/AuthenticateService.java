package com.leogouchon.squashapp.service;

import com.leogouchon.squashapp.dto.AuthenticateRequestDTO;
import com.leogouchon.squashapp.model.Users;
import com.leogouchon.squashapp.repository.UserRepository;
import com.leogouchon.squashapp.service.interfaces.IAuthenticateService;
import com.leogouchon.squashapp.service.interfaces.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.naming.AuthenticationException;
import java.util.UUID;

@Service
public class AuthenticateService implements IAuthenticateService {

    private final IUserService userService;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    @Autowired
    public AuthenticateService(IUserService userService, PasswordEncoder passwordEncoder, UserRepository userRepository) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }

    public String login(AuthenticateRequestDTO user) throws AuthenticationException {
        Users existingUser = userService.getUserByEmail(user.getEmail());
        if (existingUser == null || !matchPassword(existingUser, user.getPassword())) {
            throw new AuthenticationException("user or password incorrect");
        }
        String token = generateToken(existingUser);
        userService.updateTokenUser(existingUser);
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

    public String generateToken(Users user) {
        String token = UUID.randomUUID().toString();
        user.setToken(token);
        return userRepository.save(user).getToken();
    }

    public boolean matchPassword(Users existingUsers, String unhashedPassword) {
        return passwordEncoder.matches(unhashedPassword, existingUsers.getPassword());
    }
}
