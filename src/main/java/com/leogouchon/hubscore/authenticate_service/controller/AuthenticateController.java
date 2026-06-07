package com.leogouchon.hubscore.authenticate_service.controller;

import com.leogouchon.hubscore.authenticate_service.dto.AuthenticateRequestDTO;
import com.leogouchon.hubscore.authenticate_service.dto.AuthenticateResponseDTO;
import com.leogouchon.hubscore.authenticate_service.dto.DoubleTokenDTO;
import com.leogouchon.hubscore.authenticate_service.dto.SignInRequestDTO;
import com.leogouchon.hubscore.authenticate_service.service.AuthenticateService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.naming.AuthenticationException;
import java.time.Duration;

@Slf4j
@RestController
@RequestMapping("/api/v1/authenticate")
@Tag(name = "Authentication")
public class AuthenticateController {
    private static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";
    private static final String REFRESH_TOKEN_COOKIE_PATH = "/api/v1/authenticate";
    private static final Duration REFRESH_TOKEN_COOKIE_MAX_AGE = Duration.ofDays(30);

    private final AuthenticateService authenticateService;

    @Value("${cookie.secure}")
    private boolean cookieSecure;

    @Autowired
    public AuthenticateController(
            AuthenticateService authenticateService) {
        this.authenticateService = authenticateService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticateResponseDTO> login(@RequestBody AuthenticateRequestDTO authenticateRequestDTO, HttpServletResponse response) {
        try {
            DoubleTokenDTO doubleTokenDTO = authenticateService.login(authenticateRequestDTO);

            ResponseCookie refreshCookie = buildRefreshTokenCookie(doubleTokenDTO.getRefreshToken());

            response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

            return ResponseEntity
                    .ok()
                    .body(new AuthenticateResponseDTO(doubleTokenDTO.getAccessToken()));
        } catch (AuthenticationException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized", ex);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @CookieValue(value = REFRESH_TOKEN_COOKIE_NAME, required = false) String refreshToken,
            HttpServletResponse response
    ) {
        try {
            authenticateService.logout(refreshToken);
            ResponseCookie refreshCookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, "")
                    .maxAge(0)
                    .path(REFRESH_TOKEN_COOKIE_PATH)
                    .httpOnly(true)
                    .secure(cookieSecure)
                    .sameSite(cookieSecure ? "None" : "Lax")
                    .build();
            response.setHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Logout failed", e);
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<AuthenticateResponseDTO> signup(@RequestBody SignInRequestDTO signInRequestDTO, @RequestParam String token, HttpServletResponse response) {
        try {
            DoubleTokenDTO doubleTokenDTO = authenticateService.signUp(signInRequestDTO.getEmail(), signInRequestDTO.getPassword(), token);
            ResponseCookie refreshCookie = buildRefreshTokenCookie(doubleTokenDTO.getRefreshToken());

            response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
            return ResponseEntity
                    .ok()
                    .body(new AuthenticateResponseDTO(doubleTokenDTO.getAccessToken()));
        } catch (Exception e) {
            log.error(String.valueOf(e));
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Signup failed", e);
        }
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<AuthenticateResponseDTO> refreshToken(
            @CookieValue(value = REFRESH_TOKEN_COOKIE_NAME, required = false) String refreshToken,
            HttpServletResponse response
    ) {
        try {
            DoubleTokenDTO doubleTokenDTO = authenticateService.refreshTokens(refreshToken);
            ResponseCookie refreshCookie = buildRefreshTokenCookie(doubleTokenDTO.getRefreshToken());

            response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

            return ResponseEntity
                    .ok()
                    .body(new AuthenticateResponseDTO(doubleTokenDTO.getAccessToken()));
        } catch (AuthenticationException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized", ex);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Refresh token rejected", ex);
        }
    }

    private ResponseCookie buildRefreshTokenCookie(String refreshToken) {
        return ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, refreshToken)
                .httpOnly(true)
                .secure(cookieSecure)
                .path(REFRESH_TOKEN_COOKIE_PATH)
                .sameSite(cookieSecure ? "None" : "Lax")
                .maxAge(REFRESH_TOKEN_COOKIE_MAX_AGE)
                .build();
    }
}
