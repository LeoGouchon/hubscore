package com.leogouchon.hubscore.squash_match_service.utils;

import com.leogouchon.hubscore.player_service.entity.Players;
import com.leogouchon.hubscore.squash_common.type.PlayerRank;
import com.leogouchon.hubscore.squash_match_service.dto.BatchSessionResponseDTO;
import com.leogouchon.hubscore.squash_match_service.repository.projection.SessionMatchProjection;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class SessionRankingUtils {

    private SessionRankingUtils() {
    }

    public static BatchSessionResponseDTO toBatchSessionResponse(Long dayUnix, List<SessionMatchProjection> matches) {
        Map<UUID, PlayerRankAccumulator> playerRanks = new HashMap<>();

        for (SessionMatchProjection match : matches) {
            Players playerA = toPlayer(match.getPlayerAId(), match.getPlayerAFirstname(), match.getPlayerALastname());
            Players playerB = toPlayer(match.getPlayerBId(), match.getPlayerBFirstname(), match.getPlayerBLastname());

            PlayerRankAccumulator playerAStats = playerRanks.computeIfAbsent(playerA.getId(), ignored -> new PlayerRankAccumulator(playerA));
            PlayerRankAccumulator playerBStats = playerRanks.computeIfAbsent(playerB.getId(), ignored -> new PlayerRankAccumulator(playerB));

            playerAStats.recordMatch(playerB, match.getFinalScoreA(), match.getFinalScoreB());
            playerBStats.recordMatch(playerA, match.getFinalScoreB(), match.getFinalScoreA());
        }

        List<PlayerRank> sortedRanks = playerRanks.values().stream()
                .sorted(SessionRankingUtils::comparePlayerRanks)
                .map(PlayerRankAccumulator::toPlayerRank)
                .toList();

        return new BatchSessionResponseDTO(dayUnix, matches.size(), sortedRanks.toArray(new PlayerRank[0]));
    }

    public static int comparePlayerRanks(PlayerRankAccumulator left, PlayerRankAccumulator right) {
        int compareVictoryRate = Double.compare(right.getVictoryRate(), left.getVictoryRate());
        if (compareVictoryRate != 0) {
            return compareVictoryRate;
        }

        int compareDirectWins = Integer.compare(
                right.getWinsAgainst(left.getPlayer().getId()),
                left.getWinsAgainst(right.getPlayer().getId())
        );
        if (compareDirectWins != 0) {
            return compareDirectWins;
        }

        int comparePointsScored = Integer.compare(right.getTotalPointsScored(), left.getTotalPointsScored());
        if (comparePointsScored != 0) {
            return comparePointsScored;
        }

        return left.getPlayer().getFirstname().compareToIgnoreCase(right.getPlayer().getFirstname());
    }

    private static Players toPlayer(UUID id, String firstname, String lastname) {
        Players player = new Players();
        player.setId(id);
        player.setFirstname(firstname);
        player.setLastname(lastname);
        return player;
    }
}
