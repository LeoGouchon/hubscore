package com.leogouchon.hubscore.squash_match_service.dto;

import com.leogouchon.hubscore.common.type.MatchPoint;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

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
    @Schema(description = "Points history of the match. The match need to be finished and respect the sport rules.", example="[{\"server\":\"A\",\"receiver\":\"B\",\"serviceSide\":\"L\",\"scoreServer\":0,\"scoreReceiver\":0}]")
    private List<MatchPoint> pointsHistory;
    @Schema(description = "Final score for player A if pointHistory is not specified", example = "11")
    private Integer finalScoreA;
    @Schema(description = "Final score for player B if pointHistory is not specified", example = "7")
    private Integer finalScoreB;
}
