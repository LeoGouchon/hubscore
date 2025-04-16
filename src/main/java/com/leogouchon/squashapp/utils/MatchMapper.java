package com.leogouchon.squashapp.utils;

import com.leogouchon.squashapp.dto.MatchResponseDTO;
import com.leogouchon.squashapp.model.Matches;

public class MatchMapper {
    public static MatchResponseDTO toMatchesResponseDTO(Matches match) {
        return new MatchResponseDTO(match);
    }
}
