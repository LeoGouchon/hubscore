package com.leogouchon.hubscore.kicker_match_service.specification;

import com.leogouchon.hubscore.kicker_match_service.dto.controller_params.LogicalOperator;
import com.leogouchon.hubscore.kicker_match_service.dto.controller_params.PlayerFilterDTO;
import com.leogouchon.hubscore.kicker_match_service.dto.controller_params.PlayerGroupDTO;
import com.leogouchon.hubscore.kicker_match_service.entity.KickerMatches;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.*;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class KickerMatchSpecifications {
    private static final String PLAYER_1_OF_TEAM_A = "player1A";
    private static final String PLAYER_2_OF_TEAM_A = "player2A";
    private static final String PLAYER_1_OF_TEAM_B = "player1B";
    private static final String PLAYER_2_OF_TEAM_B = "player2B";

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
                cb.equal(root.get(PLAYER_1_OF_TEAM_A).get("id"), playerId),
                cb.equal(root.get(PLAYER_2_OF_TEAM_A).get("id"), playerId),
                cb.equal(root.get(PLAYER_1_OF_TEAM_B).get("id"), playerId),
                cb.equal(root.get(PLAYER_2_OF_TEAM_B).get("id"), playerId)
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
     * Returns a predicate that checks if two players are in the same team.
     * Expects exactly two distinct, non-null IDs.
     *
     * @param playerIds the player IDs to compare
     * @return a predicate that checks if both players are in team A or both in team B
     */
    public static Specification<KickerMatches> hasPlayersInSameTeam(List<UUID> playerIds) {
        if (isTwoDistinctPlayers(playerIds)) {
            return null;
        }
        UUID playerId1 = playerIds.get(0);
        UUID playerId2 = playerIds.get(1);

        return (root, query, cb) -> cb.or(
                cb.or(
                        cb.and(
                                cb.equal(root.get(PLAYER_1_OF_TEAM_A).get("id"), playerId1),
                                cb.equal(root.get(PLAYER_2_OF_TEAM_A).get("id"), playerId2)
                        ),
                        cb.and(
                                cb.equal(root.get(PLAYER_1_OF_TEAM_A).get("id"), playerId2),
                                cb.equal(root.get(PLAYER_2_OF_TEAM_A).get("id"), playerId1)
                        )
                ),
                cb.or(
                        cb.and(
                                cb.equal(root.get(PLAYER_1_OF_TEAM_B).get("id"), playerId1),
                                cb.equal(root.get(PLAYER_2_OF_TEAM_B).get("id"), playerId2)
                        ),
                        cb.and(
                                cb.equal(root.get(PLAYER_1_OF_TEAM_B).get("id"), playerId2),
                                cb.equal(root.get(PLAYER_2_OF_TEAM_B).get("id"), playerId1)
                        )
                )
        );
    }

    /**
     * Returns a predicate that checks if two players are on opposite teams.
     * Expects exactly two distinct, non-null IDs.
     *
     * @param playerIds the player IDs to compare
     * @return a predicate that checks if one player is in team A and the other in team B
     */
    public static Specification<KickerMatches> hasPlayersAgainst(List<UUID> playerIds) {
        if (isTwoDistinctPlayers(playerIds)) {
            return null;
        }
        UUID playerId1 = playerIds.get(0);
        UUID playerId2 = playerIds.get(1);

        return (root, query, cb) -> cb.or(
                cb.and(
                        cb.or(
                                cb.equal(root.get(PLAYER_1_OF_TEAM_A).get("id"), playerId1),
                                cb.equal(root.get(PLAYER_2_OF_TEAM_A).get("id"), playerId1)
                        ),
                        cb.or(
                                cb.equal(root.get(PLAYER_1_OF_TEAM_B).get("id"), playerId2),
                                cb.equal(root.get(PLAYER_2_OF_TEAM_B).get("id"), playerId2)
                        )
                ),
                cb.and(
                        cb.or(
                                cb.equal(root.get(PLAYER_1_OF_TEAM_A).get("id"), playerId2),
                                cb.equal(root.get(PLAYER_2_OF_TEAM_A).get("id"), playerId2)
                        ),
                        cb.or(
                                cb.equal(root.get(PLAYER_1_OF_TEAM_B).get("id"), playerId1),
                                cb.equal(root.get(PLAYER_2_OF_TEAM_B).get("id"), playerId1)
                        )
                )
        );
    }

    /**
     * Compatibility overload. Prefer {@link #hasPlayersInSameTeam(List)}.
     */
    public static Specification<KickerMatches> hasPlayersInSameTeam(UUID playerId1, UUID playerId2) {
        return hasPlayersInSameTeam(playerPair(playerId1, playerId2));
    }

    /**
     * Compatibility overload. Prefer {@link #hasPlayersAgainst(List)}.
     */
    public static Specification<KickerMatches> hasPlayersAgainst(UUID playerId1, UUID playerId2) {
        return hasPlayersAgainst(playerPair(playerId1, playerId2));
    }

    /**
     * Returns a predicate that checks if any pair of given players appears in the same team.
     *
     * @param playerIds list of player IDs
     * @return a predicate combining all pair checks with OR
     */
    public static Specification<KickerMatches> hasAnyPlayersInSameTeam(List<UUID> playerIds) {
        if (playerIds == null || playerIds.size() < 2) {
            return null;
        }
        List<Specification<KickerMatches>> pairSpecifications = new ArrayList<>();
        for (int i = 0; i < playerIds.size(); i++) {
            for (int j = i + 1; j < playerIds.size(); j++) {
                pairSpecifications.add(hasPlayersInSameTeam(playerIds.get(i), playerIds.get(j)));
            }
        }
        return pairSpecifications.stream()
                .reduce(Specification::or)
                .orElse(null);
    }

    /**
     * Returns a predicate that checks if any pair of given players appears against each other.
     *
     * @param playerIds list of player IDs
     * @return a predicate combining all pair checks with OR
     */
    public static Specification<KickerMatches> hasAnyPlayersAgainst(List<UUID> playerIds) {
        if (playerIds == null || playerIds.size() < 2) {
            return null;
        }
        List<Specification<KickerMatches>> pairSpecifications = new ArrayList<>();
        for (int i = 0; i < playerIds.size(); i++) {
            for (int j = i + 1; j < playerIds.size(); j++) {
                pairSpecifications.add(hasPlayersAgainst(playerIds.get(i), playerIds.get(j)));
            }
        }
        return pairSpecifications.stream()
                .reduce(Specification::or)
                .orElse(null);
    }

    private static List<UUID> playerPair(UUID playerId1, UUID playerId2) {
        List<UUID> playerIds = new ArrayList<>(2);
        playerIds.add(playerId1);
        playerIds.add(playerId2);
        return playerIds;
    }

    private static boolean isTwoDistinctPlayers(List<UUID> playerIds) {
        return playerIds == null
                || playerIds.size() != 2
                || playerIds.get(0) == null
                || playerIds.get(1) == null
                || playerIds.get(0).equals(playerIds.get(1));
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
        if (group == null || !group.isPlayerGroupConform()) {
            return null;
        }

        return switch (group.getOperator()) {
            case AND -> hasAllPlayers(group.getPlayerIds());
            case OR -> hasAnyPlayer(group.getPlayerIds());
            case WITH -> hasPlayersInSameTeam(group.getPlayerIds());
            case AGAINST -> hasPlayersAgainst(group.getPlayerIds());
        };
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
