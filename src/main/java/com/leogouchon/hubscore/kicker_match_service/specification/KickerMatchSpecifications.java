package com.leogouchon.hubscore.kicker_match_service.specification;

import com.leogouchon.hubscore.kicker_match_service.dto.controller_params.LogicalOperator;
import com.leogouchon.hubscore.kicker_match_service.dto.controller_params.PlayerFilterDTO;
import com.leogouchon.hubscore.kicker_match_service.dto.controller_params.PlayerGroupDTO;
import com.leogouchon.hubscore.kicker_match_service.entity.KickerMatches;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.*;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

public class KickerMatchSpecifications {
    private KickerMatchSpecifications() {
    }

    /**
     * Creates a predicate that checks if any of the IDs of the four players associated
     * with a KickerMatch are equal to the given player ID.
     *
     * @param playerId the ID of the player to search for
     * @return a predicate that checks if a KickerMatch has a player with the given ID
     */
    public static Specification<KickerMatches> hasPlayer(UUID playerId) {
        return (root, query, cb) -> cb.or(
                cb.equal(root.get("player1A").get("id"), playerId),
                cb.equal(root.get("player2A").get("id"), playerId),
                cb.equal(root.get("player1B").get("id"), playerId),
                cb.equal(root.get("player2B").get("id"), playerId)
        );
    }


    /**
     * Returns a predicate that checks if any of the IDs of the four players associated with a KickerMatch are equal to any of the given player IDs.
     *
     * @param playerIds the list of player IDs to which the predicate should check
     * @return a predicate that checks if any of the IDs of the four associated with a KickerMatch are equal to any of the given player IDs
     */
    public static Specification<KickerMatches> hasAnyPlayer(List<UUID> playerIds) {
        return playerIds.stream()
                .map(KickerMatchSpecifications::hasPlayer)
                .reduce(Specification::or)
                .orElse(null);
    }

    public static Specification<KickerMatches> hasAllPlayers(List<UUID> playerIds) {
        return playerIds.stream()
                .map(KickerMatchSpecifications::hasPlayer)
                .reduce(Specification::and)
                .orElse(null);
    }


    /**
     * Returns a predicate that checks if the createdAt timestamp of a KickerMatch is within
     * a given date.
     *
     * @param epochMillis the epoch milliseconds representing the date to check against
     * @return a predicate that checks if the createdAt timestamp of a KickerMatch is within the given date
     */
    public static Specification<KickerMatches> dateIs(Long epochMillis) {
        return (root, query, cb) -> {
            // shamelessly prompted it
            Instant instant = Instant.ofEpochMilli(epochMillis);
            LocalDate date = instant.atZone(ZoneId.systemDefault()).toLocalDate();

            Path<Timestamp> createdAt = root.get("createdAt");

            return cb.between(
                    createdAt,
                    Timestamp.valueOf(date.atStartOfDay()),
                    Timestamp.valueOf(date.plusDays(1).atStartOfDay())
            );
        };
    }

    private static Specification<KickerMatches> simplePlayerFilter(List<UUID> playerIds) {
        if (playerIds == null || playerIds.isEmpty()) {
            return null;
        }
        return hasAnyPlayer(playerIds);
    }

    private static Specification<KickerMatches> advancedPlayerFilter(PlayerFilterDTO dto) {
        if (dto == null || !dto.isPlayerFilterConform()) {
            return null;
        }

        return dto.getGroups().stream()
                .map(KickerMatchSpecifications::buildGroupSpec)
                .reduce(
                        dto.getOperator() == LogicalOperator.AND
                                ? Specification::and
                                : Specification::or
                )
                .orElse(null);
    }

    private static Specification<KickerMatches> buildGroupSpec(PlayerGroupDTO group) {
        return group.getOperator() == LogicalOperator.AND
                ? hasAllPlayers(group.getPlayerIds())
                : hasAnyPlayer(group.getPlayerIds());
    }

    private static Specification<KickerMatches> dateFilter(Long date) {
        return date == null ? null : dateIs(date);
    }

    public static Specification<KickerMatches> withFilters(
            List<UUID> playerIds,
            PlayerFilterDTO playerFilterDTO,
            Long date
    ) {
        return Specification.where(simplePlayerFilter(playerIds))
                .and(advancedPlayerFilter(playerFilterDTO))
                .and(dateFilter(date));
    }
}
