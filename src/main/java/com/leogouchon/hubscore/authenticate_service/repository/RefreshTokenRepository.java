package com.leogouchon.hubscore.authenticate_service.repository;

import com.leogouchon.hubscore.authenticate_service.entity.RefreshToken;
import com.leogouchon.hubscore.user_service.entity.Users;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);

    @Transactional
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.revoked = true WHERE rt.user = :user AND rt.revoked = false")
    void revokeAllValidTokensByUser(@Param("user") Users user);
}
