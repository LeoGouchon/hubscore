package com.leogouchon.squashapp.controller;

import com.leogouchon.squashapp.dto.AuthenticateRequestDTO;
import com.leogouchon.squashapp.dto.TokenResponseDTO;
import com.leogouchon.squashapp.model.Users;
import com.leogouchon.squashapp.service.interfaces.IAuthenticateService;
import com.leogouchon.squashapp.service.interfaces.IUserService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.naming.AuthenticationException;

@RestController
@RequestMapping("/api/authenticate")
public class AuthenticateController {
    private final IAuthenticateService authenticateService;
    private final IUserService userService;

    @Autowired
    public AuthenticateController(
            IAuthenticateService authenticateService,
            IUserService userService)
    {
        this.authenticateService = authenticateService;
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody AuthenticateRequestDTO authenticateRequestDTO) {
        try {
            String token = authenticateService.login(authenticateRequestDTO);
            return ResponseEntity.ok("{\"token\":\"" + token + "\"}");
        } catch (AuthenticationException ex) {
            return ResponseEntity.status(HttpServletResponse.SC_UNAUTHORIZED).body("{\"error\":\"" + ex.getMessage() + "\"}");
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody TokenResponseDTO tokenResponseDTO) {
        try {
            String token = tokenResponseDTO.getToken();
            authenticateService.logout(token);
            return ResponseEntity.ok().build();
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpServletResponse.SC_UNAUTHORIZED).build();
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody Users user) {
        try {
            Users newUser = userService.createUser(user);
            String token = authenticateService.generateToken(newUser);
            return ResponseEntity.ok("{\"token\":\"" + token + "\"}");
        } catch (Exception e) {
            return ResponseEntity.status(HttpServletResponse.SC_BAD_REQUEST).body("Error during signup: " + e.getMessage());
        }
    }
}
