package com.leogouchon.hubscore.squash_match_service.dto;

import com.leogouchon.hubscore.common.type.PlayerRank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BatchSessionResponseDTO {
    private Long date;
    private int matchCount;
    private PlayerRank[] rank;

    public BatchSessionResponseDTO(Long date, int matchCount, PlayerRank[] rank) {
        this.date = date;
        this.matchCount = matchCount;
        this.rank = rank;
    }

    public BatchSessionResponseDTO() {

    }
}
