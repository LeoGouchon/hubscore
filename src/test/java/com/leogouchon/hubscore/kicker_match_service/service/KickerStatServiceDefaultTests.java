package com.leogouchon.hubscore.kicker_match_service.service;

import com.leogouchon.hubscore.kicker_match_service.dto.SeasonStatsResponseDTO;
import com.leogouchon.hubscore.kicker_match_service.dto.SeasonsStatsResponseDTO;
import com.leogouchon.hubscore.kicker_match_service.repository.KickerEloRepository;
import com.leogouchon.hubscore.kicker_match_service.repository.KickerEloSeasonalRepository;
import com.leogouchon.hubscore.kicker_match_service.repository.KickerMatchRepository;
import com.leogouchon.hubscore.kicker_match_service.repository.projection.SeasonStatsProjection;
import com.leogouchon.hubscore.kicker_match_service.service.impl.KickerStatServiceDefault;
import com.leogouchon.hubscore.player_service.service.PlayerService;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class KickerStatServiceDefaultTests {

    private final KickerMatchRepository kickerMatchRepository = mock(KickerMatchRepository.class);
    private final KickerEloSeasonalRepository kickerEloSeasonalRepository = mock(KickerEloSeasonalRepository.class);
    private final EloCalculatorService eloCalculator = mock(EloCalculatorService.class);
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

    private KickerStatServiceDefault serviceForDate(LocalDate currentDate) {
        return new KickerStatServiceDefault(
                kickerMatchRepository,
                kickerEloSeasonalRepository,
                eloCalculator,
                playerService,
                kickerEloRepository
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
