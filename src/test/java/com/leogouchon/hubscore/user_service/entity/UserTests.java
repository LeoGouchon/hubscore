package com.leogouchon.husbcore.user_service.entity;

import com.leogouchon.husbcore.player_service.entity.Players;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UserTests {
    @Test
    public void testConstructor() {
        Users user = new Users("john.doe@mail.com");
        assertNotNull(user);
        assertEquals("john.doe@mail.com", user.getEmail());
    }

    @Test
    public void testConstructorWithNullEmail() {
        Exception exception = assertThrows(NullPointerException.class, () -> new Users(null));
        assertEquals("Email must not be null", exception.getMessage());
    }

    @Test
    public void testIdentityManagedUserHasNoPassword() {
        Users user = new Users("john.doe@mail.com");
        assertEquals(UserRole.USER, user.getRole());
    }

    @Test
    public void testConstructorWithPlayer() {
        Players player = new Players("John", "Doe");
        Users user = new Users("john.doe@mail.com", player);

        assertNotNull(user);
        assertEquals("john.doe@mail.com", user.getEmail());
        assertEquals(player, user.getPlayer());
    }
}
