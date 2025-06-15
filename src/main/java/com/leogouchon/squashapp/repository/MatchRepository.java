package com.leogouchon.squashapp.repository;

import com.leogouchon.squashapp.model.Matches;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;

@Repository
public interface MatchRepository extends JpaRepository<Matches, Long>, JpaSpecificationExecutor<Matches> {

    @Query(
            value = "SELECT DISTINCT CAST(EXTRACT(EPOCH FROM DATE_TRUNC('day', end_time)) AS BIGINT) AS day_unix FROM Matches ORDER BY day_unix DESC",
            countQuery = "SELECT COUNT(DISTINCT DATE_TRUNC('day', end_time)) FROM Matches",
            nativeQuery = true)
    Page<Timestamp> getDates(Pageable pageable);
}
