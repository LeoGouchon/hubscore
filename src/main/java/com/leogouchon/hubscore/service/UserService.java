package com.leogouchon.hubscore.service;

import com.leogouchon.hubscore.model.Users;
import com.leogouchon.hubscore.repository.UserRepository;
import com.leogouchon.hubscore.service.interfaces.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService implements IUserService {

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

    public Users getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    public Users updateUser(Users user) {
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

    public void deleteUser(Long id) throws RuntimeException {
        try {
            if (userRepository.existsById(id)) {
                userRepository.deleteById(id);
            } else {
                throw new RuntimeException("User not found with id: " + id);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error occurred while deleting user with id: " + id, e);
        }
    }

    public Page<Users> getUsers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return userRepository.findAll(pageable);
    }
}
