package com.leogouchon.squashapp.repository;

import com.leogouchon.squashapp.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<Users, Long> {
    Optional<Users> findByEmail(String email);

    List<Users> findByPlayerIsNotNull();
}