package com.leogouchon.hubscore.authenticate_service.controller;

import com.leogouchon.hubscore.authenticate_service.dto.InvitationRequestDTO;
import com.leogouchon.hubscore.authenticate_service.dto.InvitationResponseDTO;
import com.leogouchon.hubscore.authenticate_service.service.InvitationService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(value = "/api/v1/admin")
@Tag(name = "Invitation")
public class InvitationController {

    private final InvitationService invitationService;

    @Autowired
    public InvitationController(InvitationService invitationService) {
        this.invitationService = invitationService;
    }

    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/invitation")
    public ResponseEntity<InvitationResponseDTO> createInvitation(@RequestHeader("Authorization") String bearerToken, @RequestBody InvitationRequestDTO invitationRequestDTO) {
        String accessToken = bearerToken.replace("Bearer ", "");

        UUID playerIdUUID = UUID.fromString(invitationRequestDTO.getPlayerId());

        String token = invitationService.createInvitation(accessToken, playerIdUUID);
        return ResponseEntity.ok(new InvitationResponseDTO(token));
    }
}
