package com.leogouchon.hubscore.authenticate_service.service;

import com.leogouchon.hubscore.authenticate_service.entity.RefreshToken;
import com.leogouchon.hubscore.authenticate_service.dto.DoubleTokenDTO;
import com.leogouchon.hubscore.authenticate_service.service.impl.AuthenticateServiceDefault;
import com.leogouchon.hubscore.user_service.entity.Users;
import com.leogouchon.hubscore.authenticate_service.repository.RefreshTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.ArgumentCaptor;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.util.ReflectionTestUtils;

import javax.naming.AuthenticationException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthenticateServiceTests {
    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Spy
    @InjectMocks
    private AuthenticateServiceDefault authenticateService;

    @BeforeEach
    public void setUp() {
        ReflectionTestUtils.setField(
                authenticateService,
                "jwtSecret",
                "test_secret_key_for_hubscore_please_change_me_12345"
        );
        ReflectionTestUtils.setField(authenticateService, "jwtExpirationMs", 3600000L);
    }

    @Test
    public void testRefreshTokenFailure() {
        String refreshToken = "invalidRefreshToken";
        when(refreshTokenRepository.findByToken(refreshToken)).thenReturn(Optional.empty());
        assertThrows(AuthenticationException.class, () -> authenticateService.refreshTokens(refreshToken));
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
    public void testGenerateAndSaveRefreshTokenCreatesIndependentSessionsForSameUser() {
        Users user = new Users("john.doe@mail.com", "P@s$w0rD");

        String firstRefreshToken = authenticateService.generateAndSaveRefreshToken(user);
        String secondRefreshToken = authenticateService.generateAndSaveRefreshToken(user);

        ArgumentCaptor<RefreshToken> refreshTokenCaptor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository, times(2)).save(refreshTokenCaptor.capture());
        List<RefreshToken> savedTokens = refreshTokenCaptor.getAllValues();

        assertNotEquals(firstRefreshToken, secondRefreshToken);
        assertNotEquals(savedTokens.get(0).getSessionId(), savedTokens.get(1).getSessionId());
        assertFalse(savedTokens.get(0).isRevoked());
        assertFalse(savedTokens.get(1).isRevoked());
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
    public void testRefreshTokensRevokesCurrentRefreshTokenAndReturnsNewTokens() throws AuthenticationException {
        String refreshToken = "validRefreshToken";
        Users user = new Users("john.doe@mail.com", "P@s$w0rD");
        RefreshToken tokenEntity = new RefreshToken();
        UUID sessionId = UUID.randomUUID();
        tokenEntity.setUser(user);
        tokenEntity.setSessionId(sessionId);
        tokenEntity.setRevoked(false);
        tokenEntity.setExpiryDate(LocalDateTime.now().plusDays(1));

        when(refreshTokenRepository.findByToken(refreshToken)).thenReturn(Optional.of(tokenEntity));

        DoubleTokenDTO newTokens = authenticateService.refreshTokens(refreshToken);

        assertTrue(tokenEntity.isRevoked());
        assertNotNull(newTokens.getAccessToken());
        assertNotNull(newTokens.getRefreshToken());
        assertTrue(authenticateService.isValidToken(newTokens.getAccessToken()));
        assertEquals(sessionId, tokenEntity.getSessionId());
        ArgumentCaptor<RefreshToken> refreshTokenCaptor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository, times(2)).save(refreshTokenCaptor.capture());
        assertEquals(sessionId, refreshTokenCaptor.getAllValues().get(1).getSessionId());
        assertFalse(refreshTokenCaptor.getAllValues().get(1).isRevoked());
        verify(refreshTokenRepository).flush();
    }
}
