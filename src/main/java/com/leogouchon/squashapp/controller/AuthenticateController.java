package com.leogouchon.squashapp.controller;

import com.leogouchon.squashapp.dto.*;
import com.leogouchon.squashapp.model.Users;
import com.leogouchon.squashapp.service.interfaces.IAuthenticateService;
import com.leogouchon.squashapp.service.interfaces.IUserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.naming.AuthenticationException;
import java.time.Duration;

@Slf4j
@RestController
@RequestMapping("/api/authenticate")
@Tag(name = "Authentication")
public class AuthenticateController {
    private final IAuthenticateService authenticateService;

    @Autowired
    public AuthenticateController(
            IAuthenticateService authenticateService) {
        this.authenticateService = authenticateService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticateResponseDTO> login(@RequestBody AuthenticateRequestDTO authenticateRequestDTO, HttpServletResponse response) {
        try {
            DoubleTokenDTO doubleTokenDTO = authenticateService.login(authenticateRequestDTO);

            ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", doubleTokenDTO.getRefreshToken())
                    .httpOnly(true)
                    .secure(false) // TODO : make it prod / dev variable
                    .path("/api/authenticate/refresh-token")
                    .sameSite("Strict")
                    .maxAge(Duration.ofDays(7))
                    .build();

            response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

            return ResponseEntity
                    .ok()
                    .body(new AuthenticateResponseDTO(doubleTokenDTO.getAccessToken()));
        } catch (AuthenticationException ex) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .build();
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
                    .path("/api/authenticate/refresh-token")
                    .httpOnly(true)
                    .secure(false) // TODO : make it prod / dev variable
                    .sameSite("Strict")
                    .build();
            response.setHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpServletResponse.SC_BAD_REQUEST).build();
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<AuthenticateResponseDTO> signup(@RequestBody SignInRequestDTO signInRequestDTO) {
        try {
            DoubleTokenDTO doubleTokenDTO = authenticateService.signUp(signInRequestDTO.getEmail(), signInRequestDTO.getPassword(), signInRequestDTO.getPlayer());
            return ResponseEntity
                    .ok()
                    .body(new AuthenticateResponseDTO(doubleTokenDTO.getAccessToken()));
        } catch (Exception e) {
            log.error(String.valueOf(e));
            return ResponseEntity.status(HttpServletResponse.SC_BAD_REQUEST).build();
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
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }
}
