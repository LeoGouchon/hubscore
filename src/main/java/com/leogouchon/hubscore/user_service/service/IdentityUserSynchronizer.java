package com.leogouchon.hubscore.user_service.service;

import com.leogouchon.hubscore.player_service.entity.Players;
import com.leogouchon.hubscore.user_service.entity.Users;
import com.leogouchon.hubscore.user_service.repository.UserRepository;
import com.leogouchon.hubscore.player_service.repository.PlayerRepository;
import org.springframework.stereotype.Service;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.UUID;

@Service
public class IdentityUserSynchronizer {
    private final UserRepository users;
    private final PlayerRepository players;

    public IdentityUserSynchronizer(UserRepository users, PlayerRepository players) {
        this.users = users;
        this.players = players;
    }

    public Users synchronize(Jwt jwt) {
        UUID identityUserId = UUID.fromString(jwt.getSubject());
        Users user = users.findByIdentityUserId(identityUserId).orElse(null);
        if (user == null) {
            return null;
        }

        String email = jwt.getClaimAsString("email");
        if (email != null && !email.isBlank()) {
            user.setEmail(email);
        }

        Players player = user.getPlayer();
        if (player != null) {
            String firstname = firstClaim(jwt, "given_name", "firstName");
            String lastname = firstClaim(jwt, "family_name", "lastName");
            if (firstname != null) player.setFirstname(firstname);
            if (lastname != null) player.setLastname(lastname);
            if (firstname != null || lastname != null) players.save(player);
        }

        return users.save(user);
    }

    private static String firstClaim(Jwt jwt, String... names) {
        for (String name : names) {
            String value = jwt.getClaimAsString(name);
            if (value != null && !value.isBlank()) return value;
        }
        return null;
    }
}
