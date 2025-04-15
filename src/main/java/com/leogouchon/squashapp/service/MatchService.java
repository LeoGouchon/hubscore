package com.leogouchon.squashapp.service;

import com.leogouchon.squashapp.model.Matches;
import com.leogouchon.squashapp.model.Players;
import com.leogouchon.squashapp.repository.MatchRepository;
import com.leogouchon.squashapp.service.interfaces.IMatchService;
import com.leogouchon.squashapp.service.interfaces.IPlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MatchService implements IMatchService {

    private final MatchRepository matchRepository;
    private final IPlayerService playerService;

    @Autowired
    public MatchService(MatchRepository matchRepository, IPlayerService playerService) {
        this.matchRepository = matchRepository;
        this.playerService = playerService;
    }

    public Matches createMatch(Long player1Id, Long player2Id, String pointsHistory, Integer finalScoreA, Integer finalScoreB) {
        Optional<Players> playerA = playerService.getPlayer(player1Id);
        Optional<Players> playerB = playerService.getPlayer(player2Id);
        if (playerA.isEmpty() || playerB.isEmpty()) {
            throw new RuntimeException("Player not found");
        } else {
            if (pointsHistory != null) {
                Matches match = new Matches(playerA.get(), playerB.get(), pointsHistory);
                return matchRepository.save(match);
            }
            else if (finalScoreA != null && finalScoreB != null) {
                Matches match = new Matches(playerA.get(), playerB.get(), finalScoreA, finalScoreB);
                return matchRepository.save(match);
            }
            Matches match = new Matches(playerA.get(), playerB.get());
            return matchRepository.save(match);
        }
    }

    public String addPoint(Matches match, Players player, String serviceSide) {
        if (match == null) {
            throw new RuntimeException("Match not found");
        }
        match.addService(player, serviceSide);
        matchRepository.save(match);
        return match.getPointsHistory();
    }

    public boolean isFinished(Matches match) {
        return match.isFinished();
    }

    public void deleteMatch(Long id) {
        if (matchRepository.existsById(id)) {
            matchRepository.deleteById(id);
        } else {
            throw new RuntimeException("Player not found with id: " + id);
        }
    }

    public List<Matches> getMatches() {
        return matchRepository.findAll();
    }

    public Optional<Matches> getMatch(Long id) {
        return matchRepository.findById(id);
    }
}
