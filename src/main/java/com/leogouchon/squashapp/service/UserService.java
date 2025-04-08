package com.leogouchon.squashapp.service;

import com.leogouchon.squashapp.model.Users;
import com.leogouchon.squashapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Users createUser(Users users) {
        if (userRepository.findByEmail(users.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already exists");
        }
        users.setPassword(passwordEncoder.encode(users.getPassword()));
        users.setIsAdmin(false);
        return userRepository.save(users);
    }

    public Users getUserByEmail(String username) {
        return userRepository.findByEmail(username).orElse(null);
    }

    public Users getUserByToken(String token) {
        return userRepository.findByToken(token).orElse(null);
    }

    public Users getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    public Users updateUser(Users user) {
        System.out.println(user);
        Users existingUsers = getUserById(user.getId());
        if (user.getEmail() != null) {
            existingUsers.setEmail(user.getEmail());
        }
        if (user.getPassword() != null) {
            existingUsers.setPassword(user.getPassword());
        }
        if (user.getPlayer() != null) {
            existingUsers.setPlayer(user.getPlayer());
        }
        return userRepository.save(existingUsers);
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    public List<Users> getUsers() {
        return userRepository.findAll();
    }

    public List<Users> getUsersWithLinkedPlayer() {
        return userRepository.findByPlayerIsNotNull();
    }

    public void updateTokenUser(Users user) {
        Users existingUsers = getUserById(user.getId());
        existingUsers.setToken(user.getToken());
        userRepository.save(existingUsers);
    }

}
