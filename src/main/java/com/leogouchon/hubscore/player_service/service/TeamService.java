package com.leogouchon.hubscore.player_service.service;

import com.leogouchon.hubscore.player_service.dto.TeamResponseDTO;

import java.util.List;

public interface TeamService {
    List<TeamResponseDTO> getTeams(boolean isKicker, boolean isSquash);
}
