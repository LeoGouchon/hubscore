package com.leogouchon.hubscore.player_service.specification;

import com.leogouchon.hubscore.player_service.entity.PlayerTeam;
import com.leogouchon.hubscore.player_service.entity.Players;
import com.leogouchon.hubscore.player_service.entity.Teams;
import com.leogouchon.hubscore.user_service.entity.Users;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public class PlayerSpecifications {
    private PlayerSpecifications() {}

    public static Specification<Players> bySport(String sport) {
        return (root, query, cb) -> {
            if (!"kicker".equalsIgnoreCase(sport) && !"squash".equalsIgnoreCase(sport)) {
                return cb.conjunction();
            }

            // Players → PlayerTeam
            Join<Players, PlayerTeam> playerTeamJoin = root.join("playerTeams", JoinType.INNER);
            // PlayerTeam → Teams
            Join<PlayerTeam, Teams> teamJoin = playerTeamJoin.join("team", JoinType.INNER);

            if ("kicker".equalsIgnoreCase(sport)) {
                return cb.isTrue(teamJoin.get("isKicker"));
            } else if ("squash".equalsIgnoreCase(sport)) {
                return cb.isTrue(teamJoin.get("isSquash"));
            } else {
                return cb.conjunction();
            }
        };
    }

    public static Specification<Players> byTeam(UUID teamId) {
        if (teamId != null) {
            return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("teamId"), teamId);
        } else {
            return Specification.where(null);
        }
    }

    public static Specification<Players> withoutUser() {
        return (root, query, criteriaBuilder) -> {
            Subquery<UUID> subquery = query.subquery(UUID.class);
            Root<Users> userRoot = subquery.from(Users.class);

            subquery.select(userRoot.get("player").get("id"))
                    .where(criteriaBuilder.isNotNull(userRoot.get("player")));

            return criteriaBuilder.not(root.get("id").in(subquery));
        };
    }

}
