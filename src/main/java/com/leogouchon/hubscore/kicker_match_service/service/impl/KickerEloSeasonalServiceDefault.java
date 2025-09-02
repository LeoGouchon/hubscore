package com.leogouchon.hubscore.kicker_match_service.service.impl;

import com.leogouchon.hubscore.kicker_match_service.entity.KickerEloId;
import com.leogouchon.hubscore.kicker_match_service.entity.KickerEloSeasonal;
import com.leogouchon.hubscore.kicker_match_service.entity.KickerMatches;
import com.leogouchon.hubscore.kicker_match_service.repository.KickerEloSeasonalRepository;
import com.leogouchon.hubscore.kicker_match_service.repository.KickerMatchRepository;
import com.leogouchon.hubscore.kicker_match_service.service.KickerEloSeasonalService;
import com.leogouchon.hubscore.player_service.entity.PlayerKickerInformations;
import com.leogouchon.hubscore.player_service.entity.Players;
import com.leogouchon.hubscore.player_service.repository.PlayerKickerInformationsRepository;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class KickerEloSeasonalServiceDefault implements KickerEloSeasonalService {
    private final KickerMatchRepository matchRepository;
    private final PlayerKickerInformationsRepository playerKickerInformationsRepository;
    private final EloCalculatorDefault eloCalculator = new EloCalculatorDefault();
    private final KickerEloSeasonalRepository kickerEloSeasonalRepository;
    private final EntityManager entityManager;


    @Autowired
    public KickerEloSeasonalServiceDefault(EntityManager entityManager, KickerEloSeasonalRepository eloSeasonalRepository, KickerMatchRepository matchRepository, PlayerKickerInformationsRepository playerKickerInformationsRepository, KickerEloSeasonalRepository kickerEloSeasonalRepository) {
        this.matchRepository = matchRepository;
        this.playerKickerInformationsRepository = playerKickerInformationsRepository;
        this.kickerEloSeasonalRepository = kickerEloSeasonalRepository;
        this.entityManager = entityManager;
    }

    @Override
    @Transactional
    public void calculateElo(KickerMatches match) {
        // Get all players of the match
        List<Players> players = Stream.of(
                match.getPlayer1A(),
                match.getPlayer2A(),
                match.getPlayer1B(),
                match.getPlayer2B()
        ).filter(Objects::nonNull).toList();

        // Load current seasonal elos of each player
        Map<UUID, Integer> currentElos = players.stream()
                .collect(Collectors.toMap(
                        Players::getId,
                        p -> kickerEloSeasonalRepository.findLastEloForSeason(p.getId(), this.getSeason(match.getCreatedAt()).year, this.getSeason(match.getCreatedAt()).quarter).orElse(eloCalculator.getInitialELo())
                ));

        int scoreA = match.getScoreA();
        int scoreB = match.getScoreB();

        double actualScoreA = eloCalculator.getScore(scoreA, scoreB);
        double actualScoreB = eloCalculator.getScore(scoreB, scoreA);

        int scoreDiff = Math.abs(scoreA - scoreB);
        int dynamicK = eloCalculator.calculateK(scoreDiff);

        // Elo avg of both teams
        double eloTeamA = eloCalculator.averageElo(match.getPlayer1A(), match.getPlayer2A(), currentElos);
        double eloTeamB = eloCalculator.averageElo(match.getPlayer1B(), match.getPlayer2B(), currentElos);

        // Expected score
        double expectedA = eloCalculator.exceptedResult(eloTeamA, eloTeamB);
        double expectedB = 1 - expectedA;

        // Update elos of each player
        updatePlayerElo(match.getPlayer1A(), actualScoreA, expectedA, currentElos, match, dynamicK);
        if (match.getPlayer2A() != null)
            updatePlayerElo(match.getPlayer2A(), actualScoreA, expectedA, currentElos, match, dynamicK);

        updatePlayerElo(match.getPlayer1B(), actualScoreB, expectedB, currentElos, match, dynamicK);
        if (match.getPlayer2B() != null)
            updatePlayerElo(match.getPlayer2B(), actualScoreB, expectedB, currentElos, match, dynamicK);

    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recalculateAllElo() {
        List<PlayerKickerInformations> players = playerKickerInformationsRepository.findAll();
        for (PlayerKickerInformations player : players) {
            player.setPlayerCurrentSeasonalElo(eloCalculator.getInitialELo());
        }
        playerKickerInformationsRepository.saveAll(players);

        kickerEloSeasonalRepository.deleteAll();

        List<KickerMatches> matches = matchRepository.getAllByOrderByCreatedAtAsc();
        for (KickerMatches match : matches) {
            calculateElo(match);
        }
    }

    private void updatePlayerElo(Players player, double actualScore, double expectedScore, Map<UUID, Integer> currentElos, KickerMatches match, int dynamicK) {
        if (player == null) return;

        int before = currentElos.get(player.getId());
        int delta = eloCalculator.calculateDeltaElo(dynamicK, actualScore, expectedScore);
        int after = before + delta;

        KickerEloId id = new KickerEloId();
        id.setMatchId(match.getId());
        id.setPlayerId(player.getId());

        KickerEloSeasonal elo = new KickerEloSeasonal();
        elo.setId(id);
        elo.setMatch(match);
        elo.setPlayer(player);
        elo.setEloBeforeMatch(before);
        elo.setEloAfterMatch(after);
        elo.setEloChange(delta);
        elo.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        elo.setSeasonQuarter(this.getSeason(match.getCreatedAt()).quarter);
        elo.setSeasonYear(this.getSeason(match.getCreatedAt()).year);

        kickerEloSeasonalRepository.save(elo);

        entityManager.flush();
        entityManager.clear();

        PlayerKickerInformations info = playerKickerInformationsRepository.findById(player.getId())
                .orElseGet(() -> new PlayerKickerInformations(player));

        info.setPlayerCurrentSeasonalElo(after);

        playerKickerInformationsRepository.save(info);
    }

    public Season getSeason(Timestamp date) {
        int year = date.toLocalDateTime().getYear();
//        int quarter = ThreadLocalRandom.current().nextInt(1, 5);
        int month = date.toLocalDateTime().getMonthValue();
        int quarter = (month - 1) / 3 + 1;
        return new Season(year, quarter);
    }

    public record Season(int year, int quarter) {
    }
}
