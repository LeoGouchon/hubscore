package com.leogouchon.squashapp.controller;

import com.leogouchon.squashapp.dto.UserResponseDTO;
import com.leogouchon.squashapp.model.Users;
import com.leogouchon.squashapp.service.interfaces.IUserService;
import com.leogouchon.squashapp.utils.UsersMapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(
        value = "/api/users",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "User")
public class UserController {

    private final IUserService userService;

    @Autowired
    public UserController(IUserService userService) {
        this.userService = userService;
    }


    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> getUsers() {
        List<Users> users = userService.getUsers();
        List<UserResponseDTO> userResponseDTO = users.stream().map(UsersMapper::toUserResponseDTO).toList();
        return ResponseEntity.ok(userResponseDTO);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable Long id) {
        Optional<Users> user = Optional.ofNullable(userService.getUserById(id));
        Optional<UserResponseDTO> userResponseDTO = user.map(UsersMapper::toUserResponseDTO);
        return userResponseDTO.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Users> createUser(@RequestBody Users users) {
        if (!users.getEmail().matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            return ResponseEntity.badRequest().body(null);
        }
        Users createdUsers = userService.createUser(users);
        URI location = URI.create("/api/users/" + createdUsers.getId());
        return ResponseEntity.created(location).body(createdUsers);
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
