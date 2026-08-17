package com.leogouchon.husbcore.player_service.service;

import com.leogouchon.husbcore.player_service.dto.TeamResponseDTO;

import java.util.List;

public interface TeamService {
    List<TeamResponseDTO> getTeams(boolean isKicker, boolean isSquash);
}
