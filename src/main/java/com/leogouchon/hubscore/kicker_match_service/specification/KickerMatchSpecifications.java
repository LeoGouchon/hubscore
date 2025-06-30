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
            Long date
    ) {
        return (Root<KickerMatches> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            Predicate predicate = cb.conjunction();

            if (playerIds != null && !playerIds.isEmpty()) {
                Predicate playerA1In = root.get("player_one_team_a").get("id").in(playerIds);
                Predicate playerA2In = root.get("player_two_team_a").get("id").in(playerIds);
                Predicate playerB1In = root.get("player_one_team_b").get("id").in(playerIds);
                Predicate playerB2In = root.get("player_two_team_b").get("id").in(playerIds);
                predicate = cb.and(predicate, cb.or(playerA1In, playerA2In, playerB1In, playerB2In));
            }

            if (date != null) {
                LocalDate localDate = LocalDate.ofEpochDay(date / 86400);
                predicate = cb.and(predicate, cb.between(root.get("created_at"), Timestamp.valueOf(localDate.atStartOfDay()), Timestamp.valueOf(localDate.plusDays(1).atStartOfDay())));
            }

            return predicate;
        };
    }
}
