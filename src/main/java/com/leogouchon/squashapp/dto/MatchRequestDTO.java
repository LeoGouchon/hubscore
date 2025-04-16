package com.leogouchon.squashapp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Data Transfer Object for creating a match")
public class MatchRequestDTO {
    @NotNull
    @Schema(description = "ID of player A")
    private Long playerAId;
    @NotNull
    @Schema(description = "ID of player B")
    private Long playerBId;
    @Schema(description = "Points history of the match. The match need to be finished and respect the sport rules. The first letter A/B represent the player, the number the score BEFORE his service and L/R the service side", example = "A0L;B0R;A1R;B1R;A2R;A3L;A4R;A5L;A6R;A7L;A8R;A9L;A10R;A11", required = false)
    private String pointsHistory;
    @Schema(description = "Final score for player A if pointHistory is not specified", example = "11")
    private Integer finalScoreA;
    @Schema(description = "Final score for player B if pointHistory is not specified", example = "7")
    private Integer finalScoreB;
}
