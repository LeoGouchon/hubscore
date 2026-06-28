package com.leogouchon.hubscore.kicker_match_service.service;

import com.leogouchon.hubscore.kicker_match_service.dto.KickerMatchRequestDTO;
import com.leogouchon.hubscore.kicker_match_service.entity.KickerMatchAccessCode;
import com.leogouchon.hubscore.kicker_match_service.entity.KickerMatches;
import com.leogouchon.hubscore.kicker_match_service.repository.KickerEloRepository;
import com.leogouchon.hubscore.kicker_match_service.repository.KickerEloSeasonalRepository;
import com.leogouchon.hubscore.kicker_match_service.repository.KickerMatchRepository;
import com.leogouchon.hubscore.kicker_match_service.service.impl.KickerMatchServiceDefault;
import com.leogouchon.hubscore.player_service.entity.Players;
import com.leogouchon.hubscore.player_service.service.PlayerService;
import com.leogouchon.hubscore.user_service.entity.Users;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static com.leogouchon.hubscore.testutil.TestKickerMatchRequests.singlesRequest;
import static com.leogouchon.hubscore.testutil.TestPlayers.player;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class KickerMatchServiceDefaultPublicCreateTests {
    @Test
    void should_create_match_with_code_and_mark_code_used() {
        KickerMatchRepository matchRepository = mock(KickerMatchRepository.class);
        KickerEloRepository eloRepository = mock(KickerEloRepository.class);
        KickerEloSeasonalRepository seasonalRepository = mock(KickerEloSeasonalRepository.class);
        PlayerService playerService = mock(PlayerService.class);
        KickerEloService eloService = mock(KickerEloService.class);
        KickerEloService seasonalService = mock(KickerEloService.class);
        PlayerMatchFactsViewService playerMatchFactsViewService = mock(PlayerMatchFactsViewService.class);
        EloCalculatorService eloCalculator = mock(EloCalculatorService.class);
        KickerMatchAccessCodeService accessCodeService = mock(KickerMatchAccessCodeService.class);
        KickerMatchServiceDefault service = new KickerMatchServiceDefault(
                matchRepository,
                eloRepository,
                seasonalRepository,
                playerService,
                eloService,
                seasonalService,
                playerMatchFactsViewService,
                eloCalculator,
                accessCodeService
        );

        Players playerA = player(UUID.randomUUID());
        Players playerB = player(UUID.randomUUID());
        KickerMatchAccessCode accessCode = new KickerMatchAccessCode("PUBLIC", new Users("admin@mail.com", "password"), null);
        KickerMatchRequestDTO request = singlesRequest(playerA.getId(), playerB.getId(), 10, 6);

        when(accessCodeService.getActiveCodeForUse("public")).thenReturn(accessCode);
        when(playerService.getPlayer(playerA.getId())).thenReturn(Optional.of(playerA));
        when(playerService.getPlayer(playerB.getId())).thenReturn(Optional.of(playerB));

        KickerMatches match = service.createMatchWithCode("public", request);

        assertThat(match.getCreatedBy()).isNull();
        assertThat(match.getPlayer1A()).isSameAs(playerA);
        assertThat(match.getPlayer1B()).isSameAs(playerB);
        verify(accessCodeService).getActiveCodeForUse("public");
        verify(accessCodeService).markCodeUsed(accessCode);
        verify(matchRepository).save(match);
        verify(eloService).calculateElo(match);
        verify(seasonalService).calculateElo(match);
        verify(playerMatchFactsViewService).refreshAfterCommit();
    }
}
