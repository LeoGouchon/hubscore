package com.leogouchon.squashapp.controller;

import com.leogouchon.squashapp.model.Users;
import com.leogouchon.squashapp.service.interfaces.IAuthenticateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class MeController {

    private final IAuthenticateService authenticateService;

    @Autowired
    public MeController(IAuthenticateService authenticateService) {
        this.authenticateService = authenticateService;
    }

    @SecurityRequirement(name = "bearerAuth")
    @Tag(name = "User")
    @Operation(summary = "Return current user")
    @GetMapping("/me")
    public ResponseEntity<Users> getCurrentUser(@CookieValue(value = "refreshToken") String refreshToken) {
        Users user = authenticateService.getUserFromToken(refreshToken);
        return ResponseEntity.ok(user);
    }
}
