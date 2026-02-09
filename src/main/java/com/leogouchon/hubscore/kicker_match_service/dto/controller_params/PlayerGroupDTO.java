package com.leogouchon.hubscore.kicker_match_service.dto.controller_params;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public class PlayerGroupDTO {

    @NotNull
    private LogicalOperator operator;

    @NotEmpty
    private List<UUID> playerIds;
}
