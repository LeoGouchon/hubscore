package com.leogouchon.hubscore.player_service.service.impl;

import com.leogouchon.hubscore.player_service.dto.TeamResponseDTO;
import com.leogouchon.hubscore.player_service.repository.TeamRepository;
import com.leogouchon.hubscore.player_service.service.TeamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TeamServiceDefault implements TeamService {

    private final TeamRepository teamRepository;

    @Autowired
    public TeamServiceDefault(TeamRepository teamRepository) {
        this.teamRepository = teamRepository;
    }

    @Override
    public List<TeamResponseDTO> getTeams(boolean isKicker, boolean isSquash) {
        return teamRepository.findAll().stream()
                .filter(team -> isKicker == team.isKicker() && isSquash == team.isSquash())
                .map(TeamResponseDTO::new)
                .toList();
    }
}
