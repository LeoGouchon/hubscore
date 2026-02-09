package com.leogouchon.hubscore.kicker_match_service.dto.controller_params;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.util.List;

@Getter
public class PlayerFilterDTO {
    @NotNull
    private LogicalOperator operator;

    @NotEmpty
    private List<PlayerGroupDTO> groups;

    public boolean isPlayerFilterConform() {
        return groups.stream().allMatch(PlayerGroupDTO::isPlayerGroupConform) && operator != null;
    }
}
