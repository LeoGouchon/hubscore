package com.leogouchon.hubscore.kicker_match_service.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.leogouchon.hubscore.kicker_match_service.entity.KickerMatchAccessCode;
import com.leogouchon.hubscore.kicker_match_service.entity.KickerMatchAccessCodeRevokedReason;
import com.leogouchon.hubscore.kicker_match_service.entity.KickerMatchAccessCodeStatus;
import lombok.Getter;

import java.sql.Timestamp;
import java.util.UUID;

@Getter
public class KickerMatchAccessCodeResponseDTO {
    private final UUID id;
    private final String code;
    private final KickerMatchAccessCodeStatus status;
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private final Timestamp createdAt;
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private final Timestamp expiresAt;
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private final Timestamp lastUsedAt;
    private final Integer usageCount;
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private final Timestamp revokedAt;
    private final KickerMatchAccessCodeRevokedReason revokedReason;
    private final UUID createdByUserId;

    public KickerMatchAccessCodeResponseDTO(KickerMatchAccessCode accessCode) {
        this.id = accessCode.getId();
        this.code = accessCode.getCode();
        this.status = accessCode.getStatus();
        this.createdAt = accessCode.getCreatedAt();
        this.expiresAt = accessCode.getExpiresAt();
        this.lastUsedAt = accessCode.getLastUsedAt();
        this.usageCount = accessCode.getUsageCount();
        this.revokedAt = accessCode.getRevokedAt();
        this.revokedReason = accessCode.getRevokedReason();
        this.createdByUserId = accessCode.getCreatedBy().getId();
    }
}
