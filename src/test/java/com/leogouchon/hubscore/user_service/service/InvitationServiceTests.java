package com.leogouchon.husbcore.user_service.service;

import com.leogouchon.husbcore.user_service.entity.Users;
import com.leogouchon.husbcore.user_service.repository.UserRepository;
import com.leogouchon.husbcore.user_service.dto.InvitationRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvitationServiceTests {
    @Mock RestClient.Builder restClientBuilder;
    @Mock RestClient restClient;
    @Mock UserRepository users;

    private InvitationService service;

    @BeforeEach
    void setUp() {
        when(restClientBuilder.build()).thenReturn(restClient);
        service = new InvitationService(restClientBuilder, "http://identity", "secret", "kicker-client", users);
    }

    @Test
    void rejectsInvitationWhenPlayerAlreadyHasAUser() {
        UUID playerId = UUID.randomUUID();
        when(users.findByPlayer_Id(playerId)).thenReturn(Optional.of(new Users("user@example.com")));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> service.createInvitation(new InvitationRequest(playerId)));

        assertEquals(409, exception.getStatusCode().value());
        verifyNoInteractions(restClient);
    }

    @Test
    void rejectsInvitationWithoutPlayer() {
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> service.createInvitation(new InvitationRequest(null)));

        assertEquals(400, exception.getStatusCode().value());
        verifyNoInteractions(users, restClient);
    }
}
