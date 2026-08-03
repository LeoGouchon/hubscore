package com.leogouchon.hubscore.user_service.controller;

import com.leogouchon.hubscore.user_service.entity.UserRole;
import com.leogouchon.hubscore.user_service.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
public class RoleController {
    private final UserRepository users;
    public RoleController(UserRepository users) { this.users = users; }

    @PutMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public void updateRole(@PathVariable UUID id, @RequestBody RoleRequest request) {
        var user = users.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        try { user.setRole(UserRole.valueOf(request.role().toUpperCase())); }
        catch (IllegalArgumentException e) { throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown role"); }
        users.save(user);
    }
    public record RoleRequest(String role) {}
}
