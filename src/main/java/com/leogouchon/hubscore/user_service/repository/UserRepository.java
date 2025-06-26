package com.leogouchon.hubscore.user_service.repository;

import com.leogouchon.hubscore.user_service.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<Users, Long> {
    Optional<Users> findByEmail(String email);
}