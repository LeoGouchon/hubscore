package com.leogouchon.hubscore.kicker_match_service.service;

import com.leogouchon.hubscore.kicker_match_service.entity.KickerMatchAccessCode;
import com.leogouchon.hubscore.kicker_match_service.entity.KickerMatchAccessCodeRevokedReason;
import com.leogouchon.hubscore.kicker_match_service.entity.KickerMatchAccessCodeStatus;
import com.leogouchon.hubscore.kicker_match_service.exception.InactiveKickerMatchAccessCodeException;
import com.leogouchon.hubscore.kicker_match_service.exception.InvalidKickerMatchAccessCodeException;
import com.leogouchon.hubscore.kicker_match_service.repository.KickerMatchAccessCodeRepository;
import com.leogouchon.hubscore.kicker_match_service.service.impl.KickerMatchAccessCodeServiceDefault;
import com.leogouchon.hubscore.user_service.entity.Users;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class KickerMatchAccessCodeServiceDefaultTests {
    private KickerMatchAccessCodeRepository repository;
    private KickerMatchAccessCodeServiceDefault service;
    private Users admin;

    @BeforeEach
    void setUp() {
        repository = mock(KickerMatchAccessCodeRepository.class);
        service = new KickerMatchAccessCodeServiceDefault(repository);
        admin = new Users("admin@mail.com", "password");
    }

    @Test
    void should_create_custom_code_normalized_to_uppercase() {
        when(repository.existsByCode("SUMMER2026")).thenReturn(false);
        when(repository.save(any(KickerMatchAccessCode.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.createCode("summer2026", 8, null, admin);

        ArgumentCaptor<KickerMatchAccessCode> captor = ArgumentCaptor.forClass(KickerMatchAccessCode.class);
        verify(repository).save(captor.capture());
        KickerMatchAccessCode savedCode = captor.getValue();

        assertThat(savedCode.getCode()).isEqualTo("SUMMER2026");
        assertThat(savedCode.getStatus()).isEqualTo(KickerMatchAccessCodeStatus.ACTIVE);
        assertThat(savedCode.getUsageCount()).isZero();
    }

    @Test
    void should_generate_code_when_no_custom_code_is_provided() {
        when(repository.existsByCode(any())).thenReturn(false);
        when(repository.save(any(KickerMatchAccessCode.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.createCode(null, 12, null, admin);

        ArgumentCaptor<KickerMatchAccessCode> captor = ArgumentCaptor.forClass(KickerMatchAccessCode.class);
        verify(repository).save(captor.capture());

        assertThat(captor.getValue().getCode()).hasSize(12);
        assertThat(captor.getValue().getCode()).matches("^[A-Z0-9]+$");
    }

    @Test
    void should_reject_duplicate_custom_code() {
        when(repository.existsByCode("DUPLICATE")).thenReturn(true);

        assertThatThrownBy(() -> service.createCode("duplicate", 8, null, admin))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Access code already exists");

        verify(repository, never()).save(any());
    }

    @Test
    void should_return_active_code_for_use() {
        KickerMatchAccessCode accessCode = new KickerMatchAccessCode("ABC123", admin, null);
        when(repository.findLockedByCode("ABC123")).thenReturn(Optional.of(accessCode));

        KickerMatchAccessCode result = service.getActiveCodeForUse("abc123");

        assertThat(result).isSameAs(accessCode);
    }

    @Test
    void should_throw_when_code_does_not_exist() {
        when(repository.findLockedByCode("UNKNOWN")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getActiveCodeForUse("unknown"))
                .isInstanceOf(InvalidKickerMatchAccessCodeException.class)
                .hasMessageContaining("Invalid access code");
    }

    @Test
    void should_revoke_expired_code_and_throw_inactive_exception() {
        Timestamp expiredAt = Timestamp.from(Instant.now().minusSeconds(60));
        KickerMatchAccessCode accessCode = new KickerMatchAccessCode("EXPIRED", admin, expiredAt);
        when(repository.findLockedByCode("EXPIRED")).thenReturn(Optional.of(accessCode));

        assertThatThrownBy(() -> service.getActiveCodeForUse("expired"))
                .isInstanceOf(InactiveKickerMatchAccessCodeException.class)
                .hasMessageContaining("Access code is not active");

        assertThat(accessCode.getStatus()).isEqualTo(KickerMatchAccessCodeStatus.REVOKED);
        assertThat(accessCode.getRevokedReason()).isEqualTo(KickerMatchAccessCodeRevokedReason.EXPIRED);
        assertThat(accessCode.getRevokedAt()).isNotNull();
    }

    @Test
    void should_revoke_active_code_manually() {
        KickerMatchAccessCode accessCode = new KickerMatchAccessCode("MANUAL", admin, null);
        UUID codeId = UUID.randomUUID();
        accessCode.setId(codeId);
        when(repository.findById(accessCode.getId())).thenReturn(Optional.of(accessCode));

        service.revokeCode(codeId);

        assertThat(accessCode.getStatus()).isEqualTo(KickerMatchAccessCodeStatus.REVOKED);
        assertThat(accessCode.getRevokedReason()).isEqualTo(KickerMatchAccessCodeRevokedReason.MANUAL);
        assertThat(accessCode.getRevokedAt()).isNotNull();
    }

    @Test
    void should_mark_code_used() {
        KickerMatchAccessCode accessCode = new KickerMatchAccessCode("USED", admin, null);

        service.markCodeUsed(accessCode);

        assertThat(accessCode.getUsageCount()).isEqualTo(1);
        assertThat(accessCode.getLastUsedAt()).isNotNull();
    }

    @Test
    void should_revoke_expired_codes_when_listing() {
        KickerMatchAccessCode activeCode = new KickerMatchAccessCode("ACTIVE", admin, null);
        KickerMatchAccessCode expiredCode = new KickerMatchAccessCode(
                "EXPIRED",
                admin,
                Timestamp.from(Instant.now().minusSeconds(60))
        );
        when(repository.findAll()).thenReturn(List.of(activeCode, expiredCode));

        service.getCodes();

        assertThat(activeCode.getStatus()).isEqualTo(KickerMatchAccessCodeStatus.ACTIVE);
        assertThat(expiredCode.getStatus()).isEqualTo(KickerMatchAccessCodeStatus.REVOKED);
        assertThat(expiredCode.getRevokedReason()).isEqualTo(KickerMatchAccessCodeRevokedReason.EXPIRED);
    }
}
