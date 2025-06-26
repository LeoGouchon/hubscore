package com.leogouchon.hubscore.utils;

import com.leogouchon.hubscore.dto.MatchResponseDTO;
import com.leogouchon.hubscore.model.Matches;

public class MatchMapper {
    public static MatchResponseDTO toMatchesResponseDTO(Matches match) {
        return new MatchResponseDTO(match);
    }
}
