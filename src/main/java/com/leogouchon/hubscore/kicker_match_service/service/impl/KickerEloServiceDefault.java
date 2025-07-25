package com.leogouchon.hubscore.kicker_match_service.service.impl;

import com.leogouchon.hubscore.kicker_match_service.entity.KickerElo;
import com.leogouchon.hubscore.kicker_match_service.entity.KickerEloId;
import com.leogouchon.hubscore.kicker_match_service.entity.KickerMatches;
import com.leogouchon.hubscore.kicker_match_service.repository.KickerEloRepository;
import com.leogouchon.hubscore.kicker_match_service.repository.KickerMatchRepository;
import com.leogouchon.hubscore.kicker_match_service.service.KickerEloService;
import com.leogouchon.hubscore.player_service.entity.Players;
import com.leogouchon.hubscore.player_service.repository.PlayerRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class KickerEloServiceDefault implements KickerEloService {
    private final static int INITIAL_KICKER_ELO = 1500;

    private final KickerMatchRepository matchRepository;
    private final KickerEloRepository kickerEloRepository;
    private final PlayerRepository playerRepository;

    @Autowired
    public KickerEloServiceDefault(KickerEloRepository kickerEloRepository, PlayerRepository playerRepository, KickerMatchRepository matchRepository) {
        this.kickerEloRepository = kickerEloRepository;
        this.playerRepository = playerRepository;
        this.matchRepository = matchRepository;
    }

    public void calculateElo(KickerMatches match) {

        // Get all players of the match
        List<Players> players = Stream.of(
                match.getPlayer1A(),
                match.getPlayer2A(),
                match.getPlayer1B(),
                match.getPlayer2B()
        ).filter(Objects::nonNull).toList();

        // Load current elos of each player
        Map<UUID, Integer> currentElos = players.stream().collect(
                Collectors.toMap(
                        Players::getId,
                        Players::getKickerElo
                )
        );

        // Get actual score (1 = winner, 0 = loser, 0.5 = draw)
        int scoreA = match.getScoreA();
        int scoreB = match.getScoreB();
        double actualScoreA = scoreA > scoreB ? 1 : scoreA < scoreB ? 0 : 0.5;
        double actualScoreB = 1 - actualScoreA;

        int scoreDiff = Math.abs(scoreA - scoreB);
        int dynamicK = calculateK(scoreDiff);

        // Elo avg of both teams
        double eloTeamA = averageElo(match.getPlayer1A(), match.getPlayer2A(), currentElos);
        double eloTeamB = averageElo(match.getPlayer1B(), match.getPlayer2B(), currentElos);

        // Expected score
        double expectedA = 1 / (1 + Math.pow(10, (eloTeamB - eloTeamA) / 400.0)); // https://en.wikipedia.org/wiki/Elo_rating_system#cite_note-29
        double expectedB = 1 - expectedA;

        // Update elos of each player
        updatePlayerElo(match.getPlayer1A(), actualScoreA, expectedA, currentElos, match, dynamicK);
        if (match.getPlayer2A() != null)
            updatePlayerElo(match.getPlayer2A(), actualScoreA, expectedA, currentElos, match, dynamicK);

        updatePlayerElo(match.getPlayer1B(), actualScoreB, expectedB, currentElos, match, dynamicK);
        if (match.getPlayer2B() != null)
            updatePlayerElo(match.getPlayer2B(), actualScoreB, expectedB, currentElos, match, dynamicK);
    }

    private double averageElo(Players player1, Players player2, Map<UUID, Integer> currentElos) {
        if (player2 != null) {
            return (double) (currentElos.get(player1.getId()) + currentElos.get(player2.getId())) / 2;
        }
        return currentElos.get(player1.getId());
    }

    private int calculateK(int scoreDiff) {
        // Clamp scoreDiff between -10 and +10
        // Clamp entre 1 et 20
        int clamped = Math.max(1, Math.min(scoreDiff, 20));

        // Normalise sur [0, 1]
        double ratio = (clamped - 1) / 19.0;
        double exponent = 1;

        // K between [20, 40]
        double k = 20 + Math.pow(ratio, exponent) * (20 - 1);

        return (int) Math.round(k);
    }

    private void updatePlayerElo(Players player, double actualScore, double expectedScore, Map<UUID, Integer> currentElos, KickerMatches match, int dynamicK) {
        if (player == null) return;

        int before = currentElos.get(player.getId());
        int delta = (int) Math.round(dynamicK * (actualScore - expectedScore));
        int after = before + delta;

        KickerEloId id = new KickerEloId();
        id.setMatchId(match.getId());
        id.setPlayerId(player.getId());

        KickerElo elo = new KickerElo();
        elo.setId(id);
        elo.setMatch(match);
        elo.setPlayer(player);
        elo.setEloBeforeMatch(before);
        elo.setEloAfterMatch(after);
        elo.setEloChange(delta);
        elo.setCreatedAt(new Timestamp(System.currentTimeMillis()));

        kickerEloRepository.save(elo);

        // Save new elo into player table
        player.setKickerElo(after);
        playerRepository.save(player);
    }

    @Transactional
    public void recalculateAllElo() {
        List<Players> players = playerRepository.findAll();
        for (Players player : players) {
            player.setKickerElo(INITIAL_KICKER_ELO);
        }
        playerRepository.saveAll(players);

        kickerEloRepository.deleteAll();

        List<KickerMatches> matches = matchRepository.getAllByOrderByCreatedAtAsc();
        for (KickerMatches match : matches) {
            calculateElo(match);
        }
    }
}
