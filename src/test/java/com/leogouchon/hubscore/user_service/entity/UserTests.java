package com.leogouchon.hubscore.user_service.entity;

import com.leogouchon.hubscore.player_service.entity.Players;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UserTests {
    @Test
    public void testConstructor() {
        Users user = new Users("john.doe@mail.com", "p@s5w0rD");
        assertNotNull(user);
        assertEquals("john.doe@mail.com", user.getEmail());
        assertEquals("p@s5w0rD", user.getPassword());
    }

    @Test
    public void testConstructorWithNullEmail() {
        Exception exception = assertThrows(NullPointerException.class, () -> {
            new Users(null, "p@s5w0rD");
        });
        assertEquals("Email must not be null", exception.getMessage());
    }

    @Test
    public void testConstructorWithNullPassword() {
        Exception exception = assertThrows(NullPointerException.class, () -> {
            new Users("john.doe@mail.com", null);
        });
        assertEquals("Password must not be null", exception.getMessage());
    }

    @Test
    public void testConstructorWithPlayer() {
        Players player = new Players("John", "Doe");
        Users user = new Users("john.doe@mail.com", "p@s5w0rD", player);

        assertNotNull(user);
        assertEquals("john.doe@mail.com", user.getEmail());
        assertEquals("p@s5w0rD", user.getPassword());
        assertEquals(player, user.getPlayer());
    }
}
