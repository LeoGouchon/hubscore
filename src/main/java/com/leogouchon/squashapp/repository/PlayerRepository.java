package com.leogouchon.squashapp.repository;

import com.leogouchon.squashapp.model.Players;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlayerRepository extends JpaRepository<Players, Long> {
}
