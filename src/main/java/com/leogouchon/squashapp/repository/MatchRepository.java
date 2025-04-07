package com.leogouchon.squashapp.repository;

import com.leogouchon.squashapp.model.Matches;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MatchRepository extends JpaRepository<Matches, Long> {
}
