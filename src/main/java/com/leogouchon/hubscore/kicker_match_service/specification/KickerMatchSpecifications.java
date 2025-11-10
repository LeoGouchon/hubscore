package com.leogouchon.hubscore.kicker_match_service.specification;

import com.leogouchon.hubscore.kicker_match_service.entity.KickerMatches;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.*;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class KickerMatchSpecifications {
    private KickerMatchSpecifications() {}

    public static Specification<KickerMatches> withFilters(
            List<UUID> playerIds,
            Long date,
            String dateOrder
    ) {
        return (Root<KickerMatches> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            Predicate predicate = cb.conjunction();

            if (playerIds != null && !playerIds.isEmpty()) {
                Predicate playerA1In = root.get("player1A").get("id").in(playerIds);
                Predicate playerA2In = root.get("player2A").get("id").in(playerIds);
                Predicate playerB1In = root.get("player1B").get("id").in(playerIds);
                Predicate playerB2In = root.get("player2B").get("id").in(playerIds);
                predicate = cb.and(predicate, cb.or(playerA1In, playerA2In, playerB1In, playerB2In));
            }

            if (date != null) {
                LocalDate localDate = LocalDate.ofEpochDay(date / 86400);
                Path<Timestamp> createdAtPath = root.get("createdAt");

                Predicate dateNotNull = cb.isNotNull(createdAtPath);
                Predicate dateBetween = cb.between(
                        createdAtPath,
                        Timestamp.valueOf(localDate.atStartOfDay()),
                        Timestamp.valueOf(localDate.plusDays(1).atStartOfDay())
                );
                predicate = cb.and(predicate, dateNotNull, dateBetween);
            }

            if (dateOrder != null) {
                Path<Timestamp> createdAtPath = root.get("createdAt");

                Expression<Integer> nullOrdering = cb.<Integer>selectCase()
                        .when(cb.isNull(createdAtPath), 0)
                        .otherwise(1);

                if ("ascend".equalsIgnoreCase(dateOrder)) {
                    query.orderBy(
                            cb.asc(nullOrdering),
                            cb.asc(createdAtPath)
                    );
                } else if ("descend".equalsIgnoreCase(dateOrder)) {
                    query.orderBy(
                            cb.desc(nullOrdering),
                            cb.desc(createdAtPath)
                    );
                }
            }

            return predicate;
        };
    }
}
