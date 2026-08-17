package com.leogouchon.husbcore.user_service.repository;

import com.leogouchon.husbcore.user_service.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<Users, UUID> {
    Optional<Users> findByEmail(String email);

    Optional<Users> findByIdentityUserId(UUID identityUserId);

    Optional<Users> findByPlayer_Id(UUID playerId);
}
