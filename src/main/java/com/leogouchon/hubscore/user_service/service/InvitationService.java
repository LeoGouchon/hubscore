package com.leogouchon.hubscore.user_service.service;

import com.leogouchon.hubscore.user_service.dto.InvitationRequest;
import com.leogouchon.hubscore.user_service.dto.InvitationResponse;
import com.leogouchon.hubscore.user_service.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.HashMap;

@Service
public class InvitationService {
    private final RestClient restClient;
    private final String identityIssuer;
    private final String identityInternalUrl;
    private final String provisioningSecret;
    private final String identityClientId;
    private final UserRepository users;

    @Autowired
    public InvitationService(RestClient.Builder restClientBuilder,
                             @Value("${identity.issuer}") String identityIssuer,
                             @Value("${identity.internal-url:${identity.issuer}}") String identityInternalUrl,
                             @Value("${identity.provisioning-secret}") String provisioningSecret,
                             @Value("${identity.identity-client-id:kicker-client}") String identityClientId,
                             UserRepository users) {
        this.restClient = restClientBuilder.build();
        this.identityIssuer = identityIssuer;
        this.identityInternalUrl = identityInternalUrl;
        this.provisioningSecret = provisioningSecret;
        this.identityClientId = identityClientId;
        this.users = users;
    }

    public InvitationResponse createInvitation(InvitationRequest request) {
        if (request.playerId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A player is required");
        }
        if (users.findByPlayer_Id(request.playerId()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This player already has a user");
        }
        Map<String, Object> payload = new HashMap<>();
        payload.put("clientId", identityClientId);
        payload.put("playerId", request.playerId());
        return restClient.post()
                .uri(identityInternalUrl + "/api/internal/invitations")
                .header("X-Identity-Provisioning-Secret", provisioningSecret)
                .body(payload)
                .retrieve()
                .body(InvitationResponse.class);
    }

    // Keeps the service convenient to construct in unit tests and local callers.
    public InvitationService(RestClient.Builder restClientBuilder,
                             String identityIssuer,
                             String provisioningSecret,
                             String identityClientId,
                             UserRepository users) {
        this(restClientBuilder, identityIssuer, identityIssuer, provisioningSecret, identityClientId, users);
    }
}
