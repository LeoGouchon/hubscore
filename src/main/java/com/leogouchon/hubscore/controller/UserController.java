package com.leogouchon.hubscore.controller;

import com.leogouchon.hubscore.dto.PaginatedResponseDTO;
import com.leogouchon.hubscore.dto.UserResponseDTO;
import com.leogouchon.hubscore.model.Users;
import com.leogouchon.hubscore.service.interfaces.IUserService;
import com.leogouchon.hubscore.utils.UsersMapper;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Optional;

@RestController
@RequestMapping(
        value = "/api/users"
        )
@Tag(name = "User")
@Validated
public class UserController {

    private final IUserService userService;

    @Autowired
    public UserController(IUserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @ApiResponse(responseCode = "200", description = "Users found")
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = {@Content(schema = @Schema())})
    public ResponseEntity<PaginatedResponseDTO<UserResponseDTO>> getUsers(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(0) @Max(50) int size
    ) {
        Page<Users> usersPage = userService.getUsers(page, size);
        PaginatedResponseDTO<UserResponseDTO> response = new PaginatedResponseDTO<>(
                usersPage.getContent().stream().map(UsersMapper::toUserResponseDTO).toList(),
                usersPage.getNumber(),
                usersPage.getTotalPages(),
                usersPage.getTotalElements(),
                usersPage.getSize()
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @ApiResponse(responseCode = "200", description = "User with given id found")
    @ApiResponse(responseCode = "404", description = "User not found", content = {@Content(schema = @Schema())})
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = {@Content(schema = @Schema())})
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable Long id) {
        Optional<Users> user = Optional.ofNullable(userService.getUserById(id));
        Optional<UserResponseDTO> userResponseDTO = user.map(UsersMapper::toUserResponseDTO);
        return userResponseDTO.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    @ApiResponse(responseCode = "201", description = "User created")
    @ApiResponse(responseCode = "400", description = "Bad request", content = {@Content(schema = @Schema())})
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = {@Content(schema = @Schema())})
    public ResponseEntity<Users> createUser(@RequestBody Users users) {
        if (!users.getEmail().matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            return ResponseEntity.badRequest().body(null);
        }
        Users createdUsers = userService.createUser(users);
        URI location = URI.create("/api/users/" + createdUsers.getId());
        return ResponseEntity.created(location).body(createdUsers);
    }

    @PutMapping("/{id}")
    @ApiResponse(responseCode = "201", description = "Player updated")
    @ApiResponse(responseCode = "400", description = "Bad request", content = {@Content(schema = @Schema())})
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = {@Content(schema = @Schema())})
    public ResponseEntity<Users> updateUser(@PathVariable Long id, @RequestBody Users users) {
        users.setId(id);
        Users updatedUsers = userService.updateUser(users);
        return ResponseEntity.created(URI.create("/api/users/" + updatedUsers.getId())).body(updatedUsers);
    }

    @DeleteMapping("/{id}")
    @ApiResponse(responseCode = "204", description = "User deleted successfully")
    @ApiResponse(responseCode = "404", description = "User to delete not found", content = {@Content(schema = @Schema())})
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = {@Content(schema = @Schema())})
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
