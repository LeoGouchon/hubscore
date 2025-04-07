package com.leogouchon.squashapp.service;

import com.leogouchon.squashapp.model.User;
import com.leogouchon.squashapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User createUser(User users) {
        users.setPassword(passwordEncoder.encode(users.getPassword()));
        users.setIsAdmin(false);
        return userRepository.save(users);
    }

    public User getUserByEmail(String username) {
        return userRepository.findByEmail(username).orElse(null);
    }

    public User getUserByToken(String token) {
        return userRepository.findByToken(token).orElse(null);
    }

    public User getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    public User updateUser(User user) {
        System.out.println(user);
        User existingUsers = getUserById(user.getId());
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

    public List<User> getUsers() {
        return userRepository.findAll();
    }

    public List<User> getUsersWithLinkedPlayer() {
        return userRepository.findByPlayerIsNotNull();
    }

    public void updateTokenUser(User user) {
        User existingUsers = getUserById(user.getId());
        existingUsers.setToken(user.getToken());
        userRepository.save(existingUsers);
    }

}
