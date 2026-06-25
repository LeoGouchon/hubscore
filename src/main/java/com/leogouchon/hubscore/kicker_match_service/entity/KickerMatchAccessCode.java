package com.leogouchon.hubscore.kicker_match_service.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.leogouchon.hubscore.user_service.entity.Users;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "kicker_match_access_codes")
public class KickerMatchAccessCode {
    @Id
    @GeneratedValue
    @Column(columnDefinition = "UUID", updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private KickerMatchAccessCodeStatus status;

    @Column(name = "created_at", nullable = false)
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private Timestamp createdAt;

    @Column(name = "expires_at")
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private Timestamp expiresAt;

    @Column(name = "last_used_at")
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private Timestamp lastUsedAt;

    @Column(name = "usage_count", nullable = false)
    private Integer usageCount = 0;

    @Column(name = "revoked_at")
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private Timestamp revokedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "revoked_reason")
    private KickerMatchAccessCodeRevokedReason revokedReason;

    @ManyToOne(optional = false)
    @JoinColumn(name = "created_by", referencedColumnName = "id", nullable = false)
    private Users createdBy;

    protected KickerMatchAccessCode() {}

    public KickerMatchAccessCode(String code, Users createdBy, Timestamp expiresAt) {
        this.code = code;
        this.createdBy = createdBy;
        this.expiresAt = expiresAt;
        this.status = KickerMatchAccessCodeStatus.ACTIVE;
        this.createdAt = new Timestamp(System.currentTimeMillis());
    }

    public boolean isActive() {
        return KickerMatchAccessCodeStatus.ACTIVE.equals(status) && !isExpired();
    }

    public boolean isExpired() {
        return expiresAt != null && expiresAt.toInstant().isBefore(Instant.now());
    }

    public void markUsed() {
        this.usageCount++;
        this.lastUsedAt = new Timestamp(System.currentTimeMillis());
    }

    public void revoke(KickerMatchAccessCodeRevokedReason revokedReason) {
        this.status = KickerMatchAccessCodeStatus.REVOKED;
        this.revokedAt = new Timestamp(System.currentTimeMillis());
        this.revokedReason = revokedReason;
    }
}
