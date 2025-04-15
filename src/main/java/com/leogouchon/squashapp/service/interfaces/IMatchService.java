package com.leogouchon.squashapp.service.interfaces;

import com.leogouchon.squashapp.model.Matches;
import com.leogouchon.squashapp.model.Players;

import java.util.List;
import java.util.Optional;

public interface IMatchService {
    Matches createMatch(Long player1Id, Long player2Id, String pointsHistory, Integer finalScoreA, Integer finalScoreB);
    String addPoint(Matches match, Players player, String serviceSide);
    boolean isFinished(Matches match);
    void deleteMatch(Long id);
    List<Matches> getMatches();
    Optional<Matches> getMatch(Long id);
}
