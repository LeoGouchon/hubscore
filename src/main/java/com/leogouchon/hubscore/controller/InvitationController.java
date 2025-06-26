package com.leogouchon.hubscore.controller;

import com.leogouchon.hubscore.dto.InvitationResponseDTO;
import com.leogouchon.hubscore.service.interfaces.IInvitationService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/admin")
@Tag(name = "Invitation")
public class InvitationController {

    private final IInvitationService invitationService;

    @Autowired
    public InvitationController(IInvitationService invitationService) {
        this.invitationService = invitationService;
    }

    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/invitation")
    public ResponseEntity<InvitationResponseDTO> createInvitation(@RequestHeader("Authorization") String bearerToken, Long playerId) {
        String accessToken = bearerToken.replace("Bearer ", "");
        String token = invitationService.createInvitation(accessToken, playerId);
        return ResponseEntity.ok(new InvitationResponseDTO(token));
    }
}
