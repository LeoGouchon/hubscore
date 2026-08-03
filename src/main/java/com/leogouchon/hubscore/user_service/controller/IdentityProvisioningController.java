package com.leogouchon.hubscore.user_service.controller;

import com.leogouchon.hubscore.player_service.repository.PlayerRepository;
import com.leogouchon.hubscore.user_service.entity.Users;
import com.leogouchon.hubscore.user_service.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@RestController
@RequestMapping("/api/internal/identity-users")
public class IdentityProvisioningController {
    private final UserRepository users;
    private final PlayerRepository players;
    private final String secret;

    public IdentityProvisioningController(UserRepository users, PlayerRepository players, @Value("${hubscore.provisioning-secret}") String secret) {
        this.users = users;
        this.players = players;
        this.secret = secret;
    }

    @PostMapping
    public void provision(@RequestHeader("X-Identity-Provisioning-Secret") String provided,
                          @RequestBody ProvisionUser request) {
        if (!secret.equals(provided))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid provisioning secret");
        Users user = users.findByIdentityUserId(request.identityUserId()).orElseGet(() -> {
            Users created = new Users(request.email());
            created.setIdentityUserId(request.identityUserId());
            return created;
        });
        user.setEmail(request.email());
        if (request.playerId() != null) players.findById(request.playerId()).ifPresent(user::setPlayer);
        users.save(user);
    }

    public record ProvisionUser(UUID identityUserId, String email, UUID playerId) {
    }
}
