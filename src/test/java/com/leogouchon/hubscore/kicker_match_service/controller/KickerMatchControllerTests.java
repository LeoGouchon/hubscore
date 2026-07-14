package com.leogouchon.hubscore.kicker_match_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.leogouchon.hubscore.authenticate_service.service.AuthenticateService;
import com.leogouchon.hubscore.common.security.JwtAuthorizationFilter;
import com.leogouchon.hubscore.kicker_match_service.dto.KickerMatchRequestDTO;
import com.leogouchon.hubscore.kicker_match_service.dto.KickerMatchResponseDTO;
import com.leogouchon.hubscore.kicker_match_service.dto.PublicKickerMatchRequestDTO;
import com.leogouchon.hubscore.kicker_match_service.entity.KickerMatches;
import com.leogouchon.hubscore.kicker_match_service.exception.InvalidKickerMatchAccessCodeException;
import com.leogouchon.hubscore.kicker_match_service.service.KickerMatchService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.UUID;

import static com.leogouchon.hubscore.testutil.TestKickerMatches.singlesMatch;
import static com.leogouchon.hubscore.testutil.TestKickerMatchRequests.publicSinglesRequest;
import static com.leogouchon.hubscore.testutil.TestPlayers.alice;
import static com.leogouchon.hubscore.testutil.TestPlayers.bob;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(KickerMatchController.class)
@AutoConfigureMockMvc(addFilters = false)
class KickerMatchControllerTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private KickerMatchService matchService;

    @MockitoBean
    private AuthenticateService authenticateService;

    @MockitoBean
    private JwtAuthorizationFilter jwtAuthorizationFilter;

    @Test
    void should_create_public_match_with_access_code() throws Exception {
        UUID matchId = UUID.randomUUID();
        UUID playerAId = UUID.randomUUID();
        UUID playerBId = UUID.randomUUID();
        KickerMatches createdMatch = singlesMatch(matchId, alice(playerAId), bob(playerBId), 10, 6);

        PublicKickerMatchRequestDTO request = publicSinglesRequest("public-code", playerAId, playerBId, 10, 6);

        when(matchService.createMatchWithCode(eq("public-code"), any())).thenReturn(createdMatch);
        when(matchService.getMatchResponseDTO(matchId))
                .thenReturn(Optional.of(new KickerMatchResponseDTO(createdMatch, 0, 0)));

        mockMvc.perform(post("/api/v1/kicker/matches/public")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/kicker/matches/" + matchId))
                .andExpect(jsonPath("$.id").value(matchId.toString()))
                .andExpect(jsonPath("$.player1A.id").value(playerAId.toString()))
                .andExpect(jsonPath("$.player1B.id").value(playerBId.toString()))
                .andExpect(jsonPath("$.scoreA").value(10))
                .andExpect(jsonPath("$.scoreB").value(6));

        ArgumentCaptor<KickerMatchRequestDTO> captor = ArgumentCaptor.forClass(KickerMatchRequestDTO.class);
        verify(matchService).createMatchWithCode(eq("public-code"), captor.capture());
        assertThat(captor.getValue().getPlayer1AId()).isEqualTo(playerAId);
        assertThat(captor.getValue().getPlayer1BId()).isEqualTo(playerBId);
    }

    @Test
    void should_return_unauthorized_when_public_access_code_is_invalid() throws Exception {
        UUID playerAId = UUID.randomUUID();
        UUID playerBId = UUID.randomUUID();
        PublicKickerMatchRequestDTO request = publicSinglesRequest("bad-code", playerAId, playerBId, 10, 6);

        when(matchService.createMatchWithCode(eq("bad-code"), any()))
                .thenThrow(new InvalidKickerMatchAccessCodeException("Invalid access code"));

        mockMvc.perform(post("/api/v1/kicker/matches/public")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid access code"));
    }
}
