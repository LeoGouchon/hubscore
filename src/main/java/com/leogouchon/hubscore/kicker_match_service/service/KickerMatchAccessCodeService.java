package com.leogouchon.hubscore.kicker_match_service.service;

import com.leogouchon.hubscore.kicker_match_service.dto.KickerMatchAccessCodeResponseDTO;
import com.leogouchon.hubscore.kicker_match_service.entity.KickerMatchAccessCode;
import com.leogouchon.hubscore.user_service.entity.Users;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

public interface KickerMatchAccessCodeService {
    KickerMatchAccessCodeResponseDTO createCode(String requestedCode, int length, Timestamp expiresAt, Users createdBy);

    List<KickerMatchAccessCodeResponseDTO> getCodes();

    KickerMatchAccessCodeResponseDTO revokeCode(UUID id);

    KickerMatchAccessCode getActiveCodeForUse(String code);

    void markCodeUsed(KickerMatchAccessCode accessCode);
}
