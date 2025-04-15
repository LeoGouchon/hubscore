package com.leogouchon.squashapp.controller;

import com.leogouchon.squashapp.model.Users;
import com.leogouchon.squashapp.service.interfaces.IUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class MeController {

    private final IUserService userService;

    @Autowired
    public MeController(IUserService userService) {
        this.userService = userService;
    }

    @Tag(name = "users")
    @Operation(summary = "Return current user")
    @GetMapping("/me")
    public ResponseEntity<Users> getCurrentUser(@CookieValue(value = "token") String token) {
        return ResponseEntity.ok(userService.getUserByToken(token));
    }
}
