package com.leogouchon.hubscore.kicker_match_service.service.impl;

import com.leogouchon.hubscore.kicker_match_service.dto.KickerMatchAccessCodeResponseDTO;
import com.leogouchon.hubscore.kicker_match_service.entity.KickerMatchAccessCode;
import com.leogouchon.hubscore.kicker_match_service.entity.KickerMatchAccessCodeRevokedReason;
import com.leogouchon.hubscore.kicker_match_service.entity.KickerMatchAccessCodeStatus;
import com.leogouchon.hubscore.kicker_match_service.exception.InactiveKickerMatchAccessCodeException;
import com.leogouchon.hubscore.kicker_match_service.exception.InvalidKickerMatchAccessCodeException;
import com.leogouchon.hubscore.kicker_match_service.repository.KickerMatchAccessCodeRepository;
import com.leogouchon.hubscore.kicker_match_service.service.KickerMatchAccessCodeService;
import com.leogouchon.hubscore.user_service.entity.Users;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

@Service
public class KickerMatchAccessCodeServiceDefault implements KickerMatchAccessCodeService {
    private static final String ALPHANUMERIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final KickerMatchAccessCodeRepository accessCodeRepository;

    public KickerMatchAccessCodeServiceDefault(KickerMatchAccessCodeRepository accessCodeRepository) {
        this.accessCodeRepository = accessCodeRepository;
    }

    @Override
    @Transactional
    public KickerMatchAccessCodeResponseDTO createCode(String requestedCode, int length, Timestamp expiresAt, Users createdBy) {
        String code = resolveCode(requestedCode, length);
        KickerMatchAccessCode accessCode = accessCodeRepository.save(new KickerMatchAccessCode(code, createdBy, expiresAt));
        return new KickerMatchAccessCodeResponseDTO(accessCode);
    }

    @Override
    @Transactional
    public List<KickerMatchAccessCodeResponseDTO> getCodes() {
        return accessCodeRepository.findAll().stream()
                .peek(this::revokeIfExpired)
                .map(KickerMatchAccessCodeResponseDTO::new)
                .toList();
    }

    @Override
    @Transactional
    public KickerMatchAccessCodeResponseDTO revokeCode(UUID id) {
        KickerMatchAccessCode accessCode = accessCodeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Access code not found"));

        if (!KickerMatchAccessCodeStatus.REVOKED.equals(accessCode.getStatus())) {
            accessCode.revoke(KickerMatchAccessCodeRevokedReason.MANUAL);
        }

        return new KickerMatchAccessCodeResponseDTO(accessCode);
    }

    @Override
    @Transactional
    public KickerMatchAccessCode getActiveCodeForUse(String code) {
        KickerMatchAccessCode accessCode = accessCodeRepository.findLockedByCode(normalizeCode(code))
                .orElseThrow(() -> new InvalidKickerMatchAccessCodeException("Invalid access code"));

        revokeIfExpired(accessCode);

        if (!accessCode.isActive()) {
            throw new InactiveKickerMatchAccessCodeException("Access code is not active");
        }

        return accessCode;
    }

    @Override
    @Transactional
    public void markCodeUsed(KickerMatchAccessCode accessCode) {
        accessCode.markUsed();
    }

    private String generateUniqueCode(int length) {
        String code;
        do {
            code = generateCode(length);
        } while (accessCodeRepository.existsByCode(code));
        return code;
    }

    private String resolveCode(String requestedCode, int length) {
        if (requestedCode == null || requestedCode.isBlank()) {
            return generateUniqueCode(length);
        }

        String code = normalizeCode(requestedCode);
        if (accessCodeRepository.existsByCode(code)) {
            throw new IllegalArgumentException("Access code already exists");
        }
        return code;
    }

    private String generateCode(int length) {
        StringBuilder code = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            code.append(ALPHANUMERIC.charAt(SECURE_RANDOM.nextInt(ALPHANUMERIC.length())));
        }
        return code.toString();
    }

    private String normalizeCode(String code) {
        return code.trim().toUpperCase();
    }

    private void revokeIfExpired(KickerMatchAccessCode accessCode) {
        if (accessCode.isExpired() && !KickerMatchAccessCodeStatus.REVOKED.equals(accessCode.getStatus())) {
            accessCode.revoke(KickerMatchAccessCodeRevokedReason.EXPIRED);
        }
    }
}
