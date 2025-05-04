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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.naming.AuthenticationException;

@Slf4j
@RestController
@RequestMapping("/api/authenticate")
@Tag(name = "Authentication")
public class AuthenticateController {
    private final IAuthenticateService authenticateService;
    private final IUserService userService;

    @Autowired
    public AuthenticateController(
            IAuthenticateService authenticateService,
            IUserService userService) {
        this.authenticateService = authenticateService;
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticateResponseDTO> login(@RequestBody AuthenticateRequestDTO authenticateRequestDTO) {
        try {
            AuthenticateResponseDTO authenticateResponseDTO = authenticateService.login(authenticateRequestDTO);
            return ResponseEntity
                    .ok()
                    .body(authenticateResponseDTO);
        } catch (AuthenticationException ex) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .build();
        }
    }

    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody TokenRequestDTO tokenResponseDTO) {
        try {
            String accessToken = tokenResponseDTO.getAccessToken();
            authenticateService.logout(accessToken);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpServletResponse.SC_BAD_REQUEST).build();
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<AuthenticateResponseDTO> signup(@RequestBody SignInRequestDTO signInRequestDTO) {
        try {
            AuthenticateResponseDTO authenticateResponseDTO = authenticateService.signIn(signInRequestDTO.getEmail(), signInRequestDTO.getPassword(), signInRequestDTO.getPlayer());
            return ResponseEntity
                    .ok()
                    .body(authenticateResponseDTO);
        } catch (Exception e) {
            log.error(String.valueOf(e));
            return ResponseEntity.status(HttpServletResponse.SC_BAD_REQUEST).build();
        }
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<String> refreshToken(@RequestBody TokenRefreshRequestDTO tokenRequestDTO) {
        String refreshToken = tokenRequestDTO.getRefreshToken();

        try {
            String newAccessToken = authenticateService.refreshAccessToken(refreshToken);
            return ResponseEntity
                    .ok()
                    .body(newAccessToken);
        } catch (AuthenticationException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }
}
