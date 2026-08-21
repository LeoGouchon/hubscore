package com.leogouchon.hubscore.user_service.controller;

import com.leogouchon.hubscore.user_service.dto.InvitationRequest;
import com.leogouchon.hubscore.user_service.dto.InvitationResponse;
import com.leogouchon.hubscore.user_service.service.InvitationService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin")
public class InvitationController {
    private final InvitationService invitations;

    public InvitationController(InvitationService invitations) {
        this.invitations = invitations;
    }

    @PostMapping("/invitation")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public InvitationResponse createInvitation(@RequestBody InvitationRequest request) {
        return invitations.createInvitation(request);
    }
}
