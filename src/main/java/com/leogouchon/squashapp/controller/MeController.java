package com.leogouchon.squashapp.controller;

import com.leogouchon.squashapp.model.Users;
import com.leogouchon.squashapp.service.interfaces.IUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.naming.AuthenticationException;

@RestController
@RequestMapping("/api")
public class MeController {

    private final IUserService userService;

    @Autowired
    public MeController(IUserService userService) {
        this.userService = userService;
    }

    @SecurityRequirement(name = "bearerAuth")
    @Tag(name = "User")
    @Operation(summary = "Return current user")
    @GetMapping("/me")
    public ResponseEntity<Users> getCurrentUser(HttpServletRequest request) throws AuthenticationException {
        Users user = (Users) request.getAttribute("user");
        return ResponseEntity.ok(user);
    }
}
