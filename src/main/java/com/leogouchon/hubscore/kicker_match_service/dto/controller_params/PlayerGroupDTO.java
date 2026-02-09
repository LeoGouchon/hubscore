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
    private LogicalOperator operator;

    @NotEmpty
    private List<UUID> playerIds;

    public boolean isPlayerGroupConform() {
        return playerIds.stream().noneMatch(Objects::isNull);
    }
}
