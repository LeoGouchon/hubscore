package com.leogouchon.hubscore.kicker_match_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Data Transfer Object for creating a match")
public class KickerMatchRequestDTO {
    @NotNull
    @Schema(description = "ID of first player of team A")
    private Long player1TeamAId;
    @Schema(description = "ID of second player of team A (can be null)")
    private Long player2TeamAId;
    @NotNull
    @Schema(description = "ID of first player of team B")
    private Long player1TeamBId;
    @Schema(description = "ID of second player of team B (can be null)")
    private Long player2TeamBId;
    @Schema(description = "Final score for player A if pointHistory is not specified", example = "11")
    private Integer finalScoreTeamA;
    @Schema(description = "Final score for player B if pointHistory is not specified", example = "7")
    private Integer finalScoreTeamB;
}
