package com.leogouchon.hubscore.kicker_match_service.dto.controller_params;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class PlayerFilterDTO {
    @NotNull
    private LogicalOperator operator;

    @NotEmpty
    private List<PlayerGroupDTO> groups;
}
