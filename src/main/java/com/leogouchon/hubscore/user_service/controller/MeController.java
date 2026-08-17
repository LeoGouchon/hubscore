package com.leogouchon.hubscore.user_service.controller;

import com.leogouchon.hubscore.user_service.dto.MeResponseDTO;
import com.leogouchon.hubscore.user_service.entity.Users;
import com.leogouchon.hubscore.user_service.utils.UsersMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1")
public class MeController {

    private final com.leogouchon.hubscore.user_service.repository.UserRepository userRepository;

    @Autowired
    public MeController(com.leogouchon.hubscore.user_service.repository.UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @SecurityRequirement(name = "bearerAuth")
    @Tag(name = "User")
    @Operation(summary = "Return current user")
    @GetMapping("/me")
    public ResponseEntity<MeResponseDTO> getCurrentUser(@AuthenticationPrincipal Jwt jwt) {
        try {
            Users user = userRepository.findByIdentityUserId(java.util.UUID.fromString(jwt.getSubject()))
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            return ResponseEntity.ok(UsersMapper.toMeResponseDTO(user));
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized", e);
        }
    }


    @Tag(name = "Health")
    @Operation(summary = "Ping the server")
    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("pong");
    }
}
