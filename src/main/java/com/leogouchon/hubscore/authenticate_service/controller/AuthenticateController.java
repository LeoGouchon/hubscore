package com.leogouchon.hubscore.authenticate_service.controller;

import com.leogouchon.hubscore.authenticate_service.dto.AuthenticateRequestDTO;
import com.leogouchon.hubscore.authenticate_service.dto.AuthenticateResponseDTO;
import com.leogouchon.hubscore.authenticate_service.dto.DoubleTokenDTO;
import com.leogouchon.hubscore.authenticate_service.dto.SignInRequestDTO;
import com.leogouchon.hubscore.authenticate_service.service.AuthenticateService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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

            ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", doubleTokenDTO.getRefreshToken())
                    .httpOnly(true)
                    .secure(cookieSecure)
                    .path("/api/v1/authenticate/refresh-token")
                    .sameSite(cookieSecure ? "None" : "Lax")
                    .maxAge(Duration.ofDays(7))
                    .build();

            System.out.println(">>> login >>> refreshToken: " + doubleTokenDTO.getRefreshToken());

            response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

            return ResponseEntity
                    .ok()
                    .body(new AuthenticateResponseDTO(doubleTokenDTO.getAccessToken()));
        } catch (AuthenticationException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized", ex);
        }
    }

    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String bearerToken, HttpServletResponse response) {
        try {
            String accessToken = bearerToken.replace("Bearer ", "");
            authenticateService.logout(accessToken);
            ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", "")
                    .maxAge(0)
                    .path("/api/v1/authenticate/refresh-token")
                    .httpOnly(true)
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
            ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", doubleTokenDTO.getRefreshToken())
                    .httpOnly(true)
                    .secure(cookieSecure)
                    .path("/api/v1/authenticate/refresh-token")
                    .sameSite(cookieSecure ? "None" : "Lax")
                    .maxAge(Duration.ofDays(7))
                    .build();

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
    public ResponseEntity<AuthenticateResponseDTO> refreshToken(@CookieValue(value = "refreshToken", required = false) String refreshToken) {
        try {
            String newAccessToken = authenticateService.refreshAccessToken(refreshToken);
            return ResponseEntity
                    .ok()
                    .body(new AuthenticateResponseDTO(newAccessToken));
        } catch (AuthenticationException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized", ex);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Refresh token rejected", ex);
        }
    }
}
