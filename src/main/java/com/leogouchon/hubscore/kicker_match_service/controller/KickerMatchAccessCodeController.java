package com.leogouchon.hubscore.kicker_match_service.controller;

import com.leogouchon.hubscore.kicker_match_service.dto.KickerMatchAccessCodeCreateRequestDTO;
import com.leogouchon.hubscore.kicker_match_service.dto.KickerMatchAccessCodeResponseDTO;
import com.leogouchon.hubscore.kicker_match_service.service.KickerMatchAccessCodeService;
import com.leogouchon.hubscore.user_service.entity.Users;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(value = "/api/v1/admin/kicker-match-codes")
@Tag(name = "Kicker Match Access Codes")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public class KickerMatchAccessCodeController {
    private final KickerMatchAccessCodeService accessCodeService;

    public KickerMatchAccessCodeController(KickerMatchAccessCodeService accessCodeService) {
        this.accessCodeService = accessCodeService;
    }

    @PostMapping
    public ResponseEntity<KickerMatchAccessCodeResponseDTO> createCode(
            @Valid @RequestBody KickerMatchAccessCodeCreateRequestDTO request,
            @AuthenticationPrincipal Users createdBy
    ) {
        int length = request.getLength() == null ? 8 : request.getLength();
        return ResponseEntity.ok(accessCodeService.createCode(request.getCode(), length, request.getExpiresAt(), createdBy));
    }

    @GetMapping
    public ResponseEntity<List<KickerMatchAccessCodeResponseDTO>> getCodes() {
        return ResponseEntity.ok(accessCodeService.getCodes());
    }

    @PatchMapping("/{id}/revoke")
    public ResponseEntity<KickerMatchAccessCodeResponseDTO> revokeCode(@PathVariable UUID id) {
        return ResponseEntity.ok(accessCodeService.revokeCode(id));
    }
}
