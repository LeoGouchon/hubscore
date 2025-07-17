package com.leogouchon.hubscore.kicker_match_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Schema(description = "Data Transfer Object for creating a match")
public class KickerMatchRequestDTO {
    @NotNull
    @Schema(description = "ID of first player of team A")
    private UUID player1AId;
    @Schema(description = "ID of second player of team A (can be null)")
    private UUID player2AId;
    @NotNull
    @Schema(description = "ID of first player of team B")
    private UUID player1BId;
    @Schema(description = "ID of second player of team B (can be null)")
    private UUID player2BId;
    @Schema(description = "Final score for player A if pointHistory is not specified", example = "11")
    private Integer scoreA;
    @Schema(description = "Final score for player B if pointHistory is not specified", example = "7")
    private Integer scoreB;


    @Override
    public String toString() {
        return "KickerMatchRequestDTO{" +
                "player1AId=" + player1AId +
                ", player2AId=" + player2AId +
                ", player1BId=" + player1BId +
                ", player2BId=" + player2BId +
                ", scoreA=" + scoreA +
                ", scoreB=" + scoreB +
                '}';
    }
}
