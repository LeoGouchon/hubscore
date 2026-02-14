package com.leogouchon.hubscore.kicker_match_service.dto.controller_params;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Getter
public class PlayerGroupDTO {

    @NotNull
    private PlayerGroupOperator operator;

    @NotEmpty
    private List<UUID> playerIds;

    public boolean isPlayerGroupConform() {
        if (operator == null || playerIds == null || playerIds.isEmpty() || playerIds.stream().anyMatch(Objects::isNull)) {
            return false;
        }

        if (operator == PlayerGroupOperator.WITH || operator == PlayerGroupOperator.AGAINST) {
            return playerIds.size() == 2 && !playerIds.get(0).equals(playerIds.get(1));
        }

        return true;
    }
}
