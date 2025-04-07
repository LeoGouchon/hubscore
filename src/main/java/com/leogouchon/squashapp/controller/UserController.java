package com.leogouchon.squashapp.controller;

import com.leogouchon.squashapp.model.Users;
import com.leogouchon.squashapp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<Users>> getUsers() {
        List<Users> users = userService.getUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Users> getUserById(@PathVariable Long id) {
        Optional<Users> user = Optional.ofNullable(userService.getUserById(id));
        return user.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<Users> getUserByEmail(@PathVariable String email) {
        Users users = userService.getUserByEmail(email);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/token/{token}")
    public ResponseEntity<Users> getUserByToken(@PathVariable String token) {
        Users users = userService.getUserByToken(token);
        return ResponseEntity.ok(users);
    }

    @PostMapping
    public ResponseEntity<Users> createUser(@RequestBody Users users) {
        if (!users.getEmail().matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            return ResponseEntity.badRequest().body(null);
        }
        Users createdUsers = userService.createUser(users);
        return ResponseEntity.ok(createdUsers);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Users> updateUser(@PathVariable Long id, @RequestBody Users users) {
        users.setId(id);
        Users updatedUsers = userService.updateUser(users);
        return ResponseEntity.ok(updatedUsers);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
