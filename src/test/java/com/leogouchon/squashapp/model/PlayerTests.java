package com.leogouchon.squashapp.model;

import org.junit.jupiter.api.Test;

public class PlayerTests {

    @Test
    public void testConstructor() {
        Player player = new Player("John", "Doe");

        assert player.getFirstname().equals("John");
        assert player.getLastname().equals("Doe");
    }
}
