package com.leogouchon.hubscore.model;

import com.leogouchon.hubscore.player_service.entity.Players;
import org.junit.jupiter.api.Test;

public class PlayerTests {

    @Test
    public void testConstructor() {
        Players player = new Players("John", "Doe");

        assert player.getFirstname().equals("John");
        assert player.getLastname().equals("Doe");
    }
}
