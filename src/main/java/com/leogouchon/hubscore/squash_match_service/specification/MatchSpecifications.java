package com.leogouchon.hubscore.squash_match_service.specification;

import com.leogouchon.hubscore.squash_match_service.entity.SquashMatches;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.*;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;

public class MatchSpecifications {
    private MatchSpecifications() {}

    public static Specification<SquashMatches> withFilters(
            List<Long> playerIds,
            Long date
    ) {
        return (Root<SquashMatches> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            Predicate predicate = cb.conjunction();

            if (playerIds != null && !playerIds.isEmpty()) {
                Predicate playerAIn = root.get("playerA").get("id").in(playerIds);
                Predicate playerBIn = root.get("playerB").get("id").in(playerIds);
                predicate = cb.and(predicate, cb.or(playerAIn, playerBIn));
            }

            if (date != null) {
                LocalDate localDate = LocalDate.ofEpochDay(date / 86400);
                predicate = cb.and(predicate, cb.between(root.get("endTime"), Timestamp.valueOf(localDate.atStartOfDay()), Timestamp.valueOf(localDate.plusDays(1).atStartOfDay())));
            }

            return predicate;
        };
    }
}
