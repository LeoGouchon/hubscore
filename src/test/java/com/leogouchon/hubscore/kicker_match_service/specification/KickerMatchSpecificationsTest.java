package com.leogouchon.hubscore.kicker_match_service.specification;

import com.leogouchon.hubscore.kicker_match_service.dto.controller_params.LogicalOperator;
import com.leogouchon.hubscore.kicker_match_service.dto.controller_params.PlayerFilterDTO;
import com.leogouchon.hubscore.kicker_match_service.dto.controller_params.PlayerGroupDTO;
import com.leogouchon.hubscore.kicker_match_service.dto.controller_params.PlayerGroupOperator;
import com.leogouchon.hubscore.kicker_match_service.entity.KickerMatches;
import com.leogouchon.hubscore.kicker_match_service.repository.KickerMatchRepository;
import com.leogouchon.hubscore.player_service.entity.Players;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@TestPropertySource(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class KickerMatchSpecificationsTest {

    @Autowired
    private KickerMatchRepository repository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void hasAnyPlayer_shouldReturnOnlyMatchingMatches() {
        Fixtures fixtures = fixtures()
                .players("p1", "p2", "p3", "p4", "p5", "p6", "p7", "p8");

        KickerMatches firstExpected = fixtures.match("p1", "p2", "p3", "p4", LocalDateTime.of(2025, 1, 10, 12, 0));
        KickerMatches secondExpected = fixtures.match("p5", "p1", "p2", "p8", LocalDateTime.of(2025, 1, 12, 12, 0));
        fixtures.match("p5", "p6", "p7", "p8", LocalDateTime.of(2025, 1, 11, 12, 0));

        List<KickerMatches> result = repository.findAll(
                KickerMatchSpecifications.hasAnyPlayer(List.of(fixtures.id("p1")))
        );

        assertThat(result)
                .extracting(KickerMatches::getId)
                .containsExactlyInAnyOrder(firstExpected.getId(), secondExpected.getId());
    }

    @Test
    void hasAnyPlayer_shouldReturnOnlyMatchingMatchesFromSeveralPlayers() {
        Fixtures fixtures = fixtures()
                .players("p1", "p2", "p3", "p4", "p5", "p6", "p7", "p8");

        KickerMatches firstExcepted = fixtures.match("p1", "p2", "p3", "p4", LocalDateTime.of(2025, 1, 10, 12, 0));
        KickerMatches secondExcepted = fixtures.match("p5", "p6", "p7", "p8", LocalDateTime.of(2025, 1, 11, 12, 0));
        fixtures.match("p2", "p3", "p7",  "p8", LocalDateTime.of(2025, 1, 12, 12, 0));

        List<KickerMatches> result = repository.findAll(
                KickerMatchSpecifications.hasAnyPlayer(List.of(fixtures.id("p1"), fixtures.id("p5")))
        );

        assertThat(result)
                .extracting(KickerMatches::getId)
                .containsExactlyInAnyOrder(firstExcepted.getId(), secondExcepted.getId());
    }

    @Test
    void hasAllPlayers_shouldReturnOnlyMatchesContainingAllPlayers() {
        Fixtures fixtures = fixtures().players("p1", "p2", "p3", "p4", "p5", "p6", "p7");

        KickerMatches expected = fixtures.match("p1", "p2", "p3", "p4", LocalDateTime.of(2025, 1, 12, 12, 0));
        fixtures.match("p1", "p5", "p6", "p7", LocalDateTime.of(2025, 1, 13, 12, 0));

        List<KickerMatches> result = repository.findAll(
                KickerMatchSpecifications.hasAllPlayers(List.of(fixtures.id("p1"), fixtures.id("p3")))
        );

        assertThat(result).extracting(KickerMatches::getId).containsExactly(expected.getId());
    }

    @Test
    void hasPlayersInSameTeam_shouldReturnOnlyMatchesWhereBothPlayersAreTeammates() {
        Fixtures fixtures = fixtures().players("p1", "p2", "p3", "p4");

        KickerMatches expected = fixtures.match("p1", "p2", "p3", "p4", LocalDateTime.of(2025, 1, 14, 12, 0));
        fixtures.match("p1", "p3", "p2", "p4", LocalDateTime.of(2025, 1, 15, 12, 0));

        List<KickerMatches> result = repository.findAll(
                KickerMatchSpecifications.hasPlayersInSameTeam(List.of(fixtures.id("p1"), fixtures.id("p2")))
        );

        assertThat(result).extracting(KickerMatches::getId).containsExactly(expected.getId());
    }

    @Test
    void hasPlayersAgainst_shouldReturnOnlyMatchesWherePlayersAreOpponents() {
        Fixtures fixtures = fixtures().players("p1", "p2", "p3", "p4");

        KickerMatches expected = fixtures.match("p1", "p2", "p3", "p4", LocalDateTime.of(2025, 1, 16, 12, 0));
        fixtures.match("p1", "p3", "p2", "p4", LocalDateTime.of(2025, 1, 17, 12, 0));

        List<KickerMatches> result = repository.findAll(
                KickerMatchSpecifications.hasPlayersAgainst(List.of(fixtures.id("p1"), fixtures.id("p3")))
        );

        assertThat(result).extracting(KickerMatches::getId).containsExactly(expected.getId());
    }

    @Test
    void dateIs_shouldReturnOnlyMatchesFromExpectedDay() {
        Fixtures fixtures = fixtures().players("p1", "p2", "p3", "p4");

        KickerMatches expected = fixtures.match("p1", "p2", "p3", "p4", LocalDateTime.of(2025, 1, 18, 12, 0));
        fixtures.match("p1", "p3", "p2", "p4", LocalDateTime.of(2025, 1, 19, 12, 0));

        List<KickerMatches> result = repository.findAll(
                KickerMatchSpecifications.dateIs(dayEpoch(2025, 1, 18))
        );

        assertThat(result).extracting(KickerMatches::getId).containsExactly(expected.getId());
    }

    @Test
    void withFilters_shouldCombinePlayerAndDateFilters() {
        Fixtures fixtures = fixtures().players("p1", "p2", "p3", "p4", "p5");

        KickerMatches expected = fixtures.match("p1", "p2", "p3", "p4", LocalDateTime.of(2025, 2, 10, 12, 0));
        fixtures.match("p1", "p5", "p3", "p4", LocalDateTime.of(2025, 2, 11, 12, 0));

        List<KickerMatches> result = repository.findAll(
                KickerMatchSpecifications.withFilters(List.of(fixtures.id("p1")), null, dayEpoch(2025, 2, 10))
        );

        assertThat(result).extracting(KickerMatches::getId).containsExactly(expected.getId());
    }

    @Test
    void withFilters_shouldCombineDateAndAdvancedPlayerFilter_withAndBetweenGroups() {
        Fixtures fixtures = fixtures().players("p1", "p2", "p3", "p4", "p5");

        KickerMatches expected = fixtures.match("p1", "p2", "p3", "p4", LocalDateTime.of(2025, 3, 2, 12, 0));
        fixtures.match("p1", "p2", "p4", "p5", LocalDateTime.of(2025, 3, 2, 13, 0));
        fixtures.match("p1", "p3", "p2", "p4", LocalDateTime.of(2025, 3, 2, 14, 0));
        fixtures.match("p1", "p2", "p3", "p4", LocalDateTime.of(2025, 3, 3, 12, 0));

        PlayerFilterDTO filter = playerFilter(
                LogicalOperator.AND,
                group(PlayerGroupOperator.WITH, fixtures.id("p1"), fixtures.id("p2")),
                group(PlayerGroupOperator.AGAINST, fixtures.id("p1"), fixtures.id("p3"))
        );

        List<KickerMatches> result = repository.findAll(
                KickerMatchSpecifications.withFilters(null, filter, dayEpoch(2025, 3, 2))
        );

        assertThat(result).extracting(KickerMatches::getId).containsExactly(expected.getId());
    }

    @Test
    void withFilters_shouldApplyOrBetweenGroups_whenFilterOperatorIsOr() {
        Fixtures fixtures = fixtures().players("p1", "p2", "p3", "p4", "p5", "p6", "p7", "p8");

        KickerMatches expectedWith = fixtures.match("p1", "p2", "p3", "p4", LocalDateTime.of(2025, 3, 5, 10, 0));
        KickerMatches expectedAgainst = fixtures.match("p6", "p3", "p7", "p8", LocalDateTime.of(2025, 3, 5, 11, 0));
        fixtures.match("p1", "p3", "p2", "p4", LocalDateTime.of(2025, 3, 5, 12, 0));

        PlayerFilterDTO filter = playerFilter(
                LogicalOperator.OR,
                group(PlayerGroupOperator.WITH, fixtures.id("p1"), fixtures.id("p2")),
                group(PlayerGroupOperator.AGAINST, fixtures.id("p6"), fixtures.id("p7"))
        );

        List<KickerMatches> result = repository.findAll(
                KickerMatchSpecifications.withFilters(null, filter, dayEpoch(2025, 3, 5))
        );

        assertThat(result)
                .extracting(KickerMatches::getId)
                .containsExactlyInAnyOrder(expectedWith.getId(), expectedAgainst.getId());
    }

    @Test
    void withFilters_shouldRequireSimplePlayerIdsAlongsideAdvancedFilter() {
        Fixtures fixtures = fixtures().players("p1", "p2", "p3", "p4", "p5", "p6", "p7", "p8");

        KickerMatches expected = fixtures.match("p1", "p2", "p3", "p4", LocalDateTime.of(2025, 3, 8, 10, 0));
        fixtures.match("p5", "p6", "p7", "p8", LocalDateTime.of(2025, 3, 8, 11, 0));
        fixtures.match("p1", "p2", "p4", "p5", LocalDateTime.of(2025, 3, 8, 12, 0));

        PlayerFilterDTO filter = playerFilter(
                LogicalOperator.OR,
                group(PlayerGroupOperator.WITH, fixtures.id("p5"), fixtures.id("p6")),
                group(PlayerGroupOperator.AGAINST, fixtures.id("p1"), fixtures.id("p3"))
        );

        List<KickerMatches> result = repository.findAll(
                KickerMatchSpecifications.withFilters(List.of(fixtures.id("p1")), filter, dayEpoch(2025, 3, 8))
        );

        assertThat(result).extracting(KickerMatches::getId).containsExactly(expected.getId());
    }

    @Test
    void hasPlayersInSameTeam_shouldReturnNull_whenInputIsInvalid() {
        UUID playerId = UUID.randomUUID();
        assertThat(KickerMatchSpecifications.hasPlayersInSameTeam(List.of(playerId, playerId))).isNull();
    }

    @Test
    void hasPlayersAgainst_shouldReturnNull_whenInputIsInvalid() {
        UUID playerId = UUID.randomUUID();
        assertThat(KickerMatchSpecifications.hasPlayersAgainst(List.of(playerId, playerId))).isNull();
    }

    private Fixtures fixtures() {
        return new Fixtures();
    }

    private long dayEpoch(int year, int month, int day) {
        return Timestamp.valueOf(LocalDateTime.of(year, month, day, 0, 0)).getTime();
    }

    private PlayerFilterDTO playerFilter(LogicalOperator operator, PlayerGroupDTO... groups) {
        PlayerFilterDTO dto = new PlayerFilterDTO();
        ReflectionTestUtils.setField(dto, "operator", operator);
        ReflectionTestUtils.setField(dto, "groups", Arrays.asList(groups));
        return dto;
    }

    private PlayerGroupDTO group(PlayerGroupOperator operator, UUID... playerIds) {
        PlayerGroupDTO dto = new PlayerGroupDTO();
        ReflectionTestUtils.setField(dto, "operator", operator);
        ReflectionTestUtils.setField(dto, "playerIds", Arrays.asList(playerIds));
        return dto;
    }

    private class Fixtures {
        private final Map<String, Players> players = new LinkedHashMap<>();

        private Fixtures players(String... names) {
            for (String name : names) {
                Players player = new Players("first-" + name, "last-" + name);
                players.put(name, entityManager.persist(player));
            }
            return this;
        }

        private UUID id(String name) {
            return player(name).getId();
        }

        private KickerMatches match(String p1A, String p2A, String p1B, String p2B, LocalDateTime createdAt) {
            KickerMatches match = new KickerMatches(player(p1A), player(p2A), player(p1B), player(p2B), 10, 8, null);
            match.setCreatedAt(Timestamp.valueOf(createdAt));
            entityManager.persist(match);
            entityManager.flush();
            return match;
        }

        private Players player(String name) {
            Players player = players.get(name);
            assertThat(player)
                    .as("Fixture player '%s' must be created via fixtures().players(...)", name)
                    .isNotNull();
            return player;
        }
    }
}
