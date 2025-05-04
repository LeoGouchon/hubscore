package com.leogouchon.squashapp.service;

import com.leogouchon.squashapp.model.RefreshToken;
import com.leogouchon.squashapp.model.Users;
import com.leogouchon.squashapp.repository.RefreshTokenRepository;
import com.leogouchon.squashapp.repository.UserRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.naming.AuthenticationException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AuthenticateServiceTests {

    @Mock
    private UserService userService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Spy
    @InjectMocks
    private AuthenticateService authenticateService;

    @Test
    public void testRefreshTokenFailure() {
        String refreshToken = "invalidRefreshToken";
        when(refreshTokenRepository.findByToken(refreshToken)).thenReturn(Optional.empty());
        assertThrows(AuthenticationException.class, () -> authenticateService.refreshAccessToken(refreshToken));
    }

    @Test
    public void testGenerateAccessToken() {
        Users user = new Users("john.doe@mail.com", "P@s$w0rD");
        String token = authenticateService.generateAccessToken(user);

        assertNotNull(token);
        assertTrue(authenticateService.isValidToken(token));
    }

    @Test
    public void testGenerateAndSaveRefreshToken() {
        Users user = new Users("john.doe@mail.com", "P@s$w0rD");
        String refreshToken = authenticateService.generateAndSaveRefreshToken(user);

        assertNotNull(refreshToken);
        // Additional checks can be added to verify the token is saved in the repository
    }

    @Test
    public void testIsValidToken() {
        Users user = new Users("john.doe@mail.com", "P@s$w0rD");
        String token = authenticateService.generateAccessToken(user);

        assertTrue(authenticateService.isValidToken(token));
        assertFalse(authenticateService.isValidToken("invalidToken"));
    }

    @Test
    public void testLogout() {
        String refreshToken = "refreshTokenValue";
        RefreshToken refreshTokenEntity = new RefreshToken();
        refreshTokenEntity.setRevoked(false);

        when(refreshTokenRepository.findByToken(refreshToken)).thenReturn(Optional.of(refreshTokenEntity));

        authenticateService.logout(refreshToken);

        verify(refreshTokenRepository).save(refreshTokenEntity);
        assertTrue(refreshTokenEntity.isRevoked());
    }

    @Test
    public void testRefreshAccessToken() throws AuthenticationException {
        String refreshToken = "validRefreshToken";

        when(refreshTokenRepository.findByToken(refreshToken)).thenReturn(Optional.of(new RefreshToken()));
        when(authenticateService.refreshAccessToken(any(String.class))).thenReturn("newAccessToken");

        String newToken = authenticateService.refreshAccessToken(refreshToken);

        assertNotNull(newToken);
    }
}
