package com.leogouchon.squashapp.controller;

import com.leogouchon.squashapp.dto.MeResponseDTO;
import com.leogouchon.squashapp.model.Users;
import com.leogouchon.squashapp.repository.UserRepository;
import com.leogouchon.squashapp.service.interfaces.IAuthenticateService;
import com.leogouchon.squashapp.utils.UsersMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class MeController {

    private final IAuthenticateService authenticateService;

    @Autowired
    public MeController(IAuthenticateService authenticateService, UserRepository usersRepository) {
        this.authenticateService = authenticateService;
    }

    @SecurityRequirement(name = "bearerAuth")
    @Tag(name = "User")
    @Operation(summary = "Return current user")
    @GetMapping("/me")
    public ResponseEntity<MeResponseDTO> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
            Users user = authenticateService.getUserFromToken(token);
            return ResponseEntity.ok(UsersMapper.toMeResponseDTO(user));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}
