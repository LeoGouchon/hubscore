package com.leogouchon.hubscore.kicker_match_service.service;

import com.leogouchon.hubscore.kicker_match_service.dto.GlobalStatsWithHistoryDTO;
import com.leogouchon.hubscore.kicker_match_service.dto.SeasonStatsResponseDTO;
import com.leogouchon.hubscore.kicker_match_service.dto.SeasonsStatsResponseDTO;
import com.leogouchon.hubscore.kicker_match_service.repository.KickerEloRepository;
import com.leogouchon.hubscore.kicker_match_service.repository.KickerEloSeasonalRepository;
import com.leogouchon.hubscore.kicker_match_service.repository.KickerMatchRepository;
import com.leogouchon.hubscore.kicker_match_service.repository.projection.GlobalStatsResponseProjection;
import com.leogouchon.hubscore.kicker_match_service.repository.projection.LastKickerEloByDateProjection;
import com.leogouchon.hubscore.kicker_match_service.repository.projection.SeasonStatsProjection;
import com.leogouchon.hubscore.kicker_match_service.service.impl.KickerStatServiceDefault;
import com.leogouchon.hubscore.player_service.service.PlayerService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.sql.Timestamp;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class KickerStatServiceDefaultTests {

    private final KickerMatchRepository kickerMatchRepository = mock(KickerMatchRepository.class);
    private final KickerEloSeasonalRepository kickerEloSeasonalRepository = mock(KickerEloSeasonalRepository.class);
    private final PlayerService playerService = mock(PlayerService.class);
    private final KickerEloRepository kickerEloRepository = mock(KickerEloRepository.class);

    @Test
    void getSeasonsStatsIncludesCurrentSeasonWhenItHasNoMatch() {
        KickerStatServiceDefault service = serviceForDate(LocalDate.of(2026, 7, 1));
        when(kickerEloSeasonalRepository.getNbSeasons()).thenReturn(1);
        when(kickerMatchRepository.getTotalMatches()).thenReturn(12);
        when(kickerMatchRepository.getTotalPlayers()).thenReturn(4);
        when(kickerEloSeasonalRepository.getSeasonsStats()).thenReturn(List.of(season(2026, 2, 12, 4)));

        SeasonsStatsResponseDTO response = service.getSeasonsStats();

        assertThat(response.getNbSeasons()).isEqualTo(2);
        assertThat(response.getTotalMatches()).isEqualTo(12);
        assertThat(response.getTotalPlayers()).isEqualTo(4);
        assertThat(response.getSeasonsStats())
                .extracting(
                        SeasonStatsResponseDTO::getYear,
                        SeasonStatsResponseDTO::getQuarter,
                        SeasonStatsResponseDTO::getNbMatches,
                        SeasonStatsResponseDTO::getNbPlayers
                )
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple(2026, 3, 0, 0),
                        org.assertj.core.groups.Tuple.tuple(2026, 2, 12, 4)
                );
    }

    @Test
    void getSeasonsStatsDoesNotDuplicateCurrentSeasonWhenItAlreadyExists() {
        KickerStatServiceDefault service = serviceForDate(LocalDate.of(2026, 7, 15));
        when(kickerEloSeasonalRepository.getNbSeasons()).thenReturn(1);
        when(kickerMatchRepository.getTotalMatches()).thenReturn(3);
        when(kickerMatchRepository.getTotalPlayers()).thenReturn(4);
        when(kickerEloSeasonalRepository.getSeasonsStats()).thenReturn(List.of(season(2026, 3, 3, 4)));

        SeasonsStatsResponseDTO response = service.getSeasonsStats();

        assertThat(response.getNbSeasons()).isEqualTo(1);
        assertThat(response.getSeasonsStats()).hasSize(1);
        assertThat(response.getSeasonsStats().getFirst().getYear()).isEqualTo(2026);
        assertThat(response.getSeasonsStats().getFirst().getQuarter()).isEqualTo(3);
        assertThat(response.getSeasonsStats().getFirst().getNbMatches()).isEqualTo(3);
        assertThat(response.getSeasonsStats().getFirst().getNbPlayers()).isEqualTo(4);
    }

    @Test
    void should_pass_active_since_90_days_before_now_when_loading_global_ranking() {
        Instant now = Instant.parse("2026-11-25T11:00:00Z");
        KickerStatServiceDefault service = serviceAt(now);

        service.getGlobalStats();

        verify(kickerMatchRepository).getGlobalKickerStats(
                null,
                null,
                10,
                LocalDateTime.ofInstant(now, ZoneId.systemDefault()).minusDays(90)
        );
    }

    @Test
    void should_pass_active_since_90_days_before_cutoff_when_loading_last_week_ranking() {
        KickerStatServiceDefault service = serviceAt(Instant.parse("2026-11-25T11:00:00Z"));
        ArgumentCaptor<Timestamp> date = ArgumentCaptor.forClass(Timestamp.class);
        ArgumentCaptor<LocalDateTime> activeSince = ArgumentCaptor.forClass(LocalDateTime.class);

        service.getGlobalStats();

        verify(kickerMatchRepository).getLatestKickerEloByDate(date.capture(), eq(10), activeSince.capture());
        LocalDateTime cutoff = date.getValue().toLocalDateTime();
        assertThat(cutoff.getDayOfWeek()).isEqualTo(DayOfWeek.MONDAY);
        assertThat(cutoff.toLocalTime()).isEqualTo(LocalTime.MIDNIGHT);
        assertThat(activeSince.getValue()).isEqualTo(cutoff.minusDays(90));
    }

    @Test
    void should_fill_elo_last_week_without_rank_last_week_when_player_was_inactive_at_cutoff() {
        UUID playerId = UUID.randomUUID();
        givenGlobalRanking(playerId, 1, 1580);
        givenLastWeekRanking(playerId, 1550, null);

        GlobalStatsWithHistoryDTO dto = serviceAt(Instant.parse("2026-11-25T11:00:00Z")).getGlobalStats().getFirst();

        assertThat(dto.getRank()).isEqualTo(1);
        assertThat(dto.getEloLastWeek()).isEqualTo(1550);
        assertThat(dto.getRankLastWeek()).isNull();
    }

    @Test
    void should_fill_rank_last_week_when_player_was_ranked_at_cutoff() {
        UUID playerId = UUID.randomUUID();
        givenGlobalRanking(playerId, 1, 1580);
        givenLastWeekRanking(playerId, 1550, 2);

        GlobalStatsWithHistoryDTO dto = serviceAt(Instant.parse("2026-11-25T11:00:00Z")).getGlobalStats().getFirst();

        assertThat(dto.getRankLastWeek()).isEqualTo(2);
        assertThat(dto.getEloLastWeek()).isEqualTo(1550);
    }

    @Test
    void should_leave_last_week_values_empty_when_player_is_absent_at_cutoff() {
        UUID playerId = UUID.randomUUID();
        givenGlobalRanking(playerId, 1, 1580);
        when(kickerMatchRepository.getLatestKickerEloByDate(any(), anyInt(), any())).thenReturn(List.of());

        GlobalStatsWithHistoryDTO dto = serviceAt(Instant.parse("2026-11-25T11:00:00Z")).getGlobalStats().getFirst();

        assertThat(dto.getRankLastWeek()).isNull();
        assertThat(dto.getEloLastWeek()).isNull();
    }

    private void givenGlobalRanking(UUID playerId, int rank, Integer currentElo) {
        GlobalStatsResponseProjection stat = mock(GlobalStatsResponseProjection.class);
        when(stat.getPlayerId()).thenReturn(playerId);
        when(stat.getRank()).thenReturn(rank);
        when(stat.getCurrentElo()).thenReturn(currentElo);
        when(kickerMatchRepository.getGlobalKickerStats(any(), any(), anyInt(), any())).thenReturn(List.of(stat));
        when(kickerMatchRepository.getLastFiveResultsByPlayerId(playerId)).thenReturn(List.of());
    }

    private void givenLastWeekRanking(UUID playerId, int elo, Integer rank) {
        LastKickerEloByDateProjection lastWeek = mock(LastKickerEloByDateProjection.class);
        when(lastWeek.getPlayerId()).thenReturn(playerId);
        when(lastWeek.getElo()).thenReturn(elo);
        when(lastWeek.getRank()).thenReturn(rank);
        when(kickerMatchRepository.getLatestKickerEloByDate(any(), anyInt(), any())).thenReturn(List.of(lastWeek));
    }

    private KickerStatServiceDefault serviceAt(Instant now) {
        return new KickerStatServiceDefault(
                kickerMatchRepository,
                kickerEloSeasonalRepository,
                playerService,
                kickerEloRepository,
                Clock.fixed(now, ZoneOffset.UTC)
        );
    }

    private KickerStatServiceDefault serviceForDate(LocalDate currentDate) {
        return new KickerStatServiceDefault(
                kickerMatchRepository,
                kickerEloSeasonalRepository,
                playerService,
                kickerEloRepository,
                Clock.systemUTC()
        ) {
            @Override
            protected LocalDate currentDate() {
                return currentDate;
            }
        };
    }

    private SeasonStatsProjection season(int year, int quarter, int nbMatches, int nbPlayers) {
        return new SeasonStatsProjection() {
            @Override
            public Integer getYear() {
                return year;
            }

            @Override
            public Integer getQuarter() {
                return quarter;
            }

            @Override
            public Integer getNbMatches() {
                return nbMatches;
            }

            @Override
            public Integer getNbPlayers() {
                return nbPlayers;
            }
        };
    }
}
