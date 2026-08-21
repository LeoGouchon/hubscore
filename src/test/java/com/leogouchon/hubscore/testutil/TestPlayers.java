package com.leogouchon.hubscore.testutil;

import com.leogouchon.hubscore.player_service.entity.Players;

import java.util.UUID;

public final class TestPlayers {
    private TestPlayers() {
    }

    public static Players player(UUID id) {
        Players player = new Players();
        player.setId(id);
        return player;
    }

    public static Players player(UUID id, String firstname, String lastname) {
        Players player = new Players(firstname, lastname);
        player.setId(id);
        return player;
    }

    public static Players alice(UUID id) {
        return player(id, "Alice", "A");
    }

    public static Players bob(UUID id) {
        return player(id, "Bob", "B");
    }
}
