package com.leogouchon.squashapp.model;

import com.leogouchon.squashapp.enums.ServiceSide;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Objects;

@Setter
@Getter
@Entity
@Table(name = "matches")
public class Match {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String pointsHistory;
    private Integer finalScoreA;
    private Integer finalScoreB;
    private Timestamp startTime;
    private Timestamp endTime;

    @OneToOne
    @JoinColumn(name = "playerA_id", referencedColumnName = "id")
    private Player playerA;

    @OneToOne
    @JoinColumn(name = "playerB_id", referencedColumnName = "id")
    private Player playerB;

    @Deprecated
    protected Match() {
    }

    public Match(Player playerA, Player playerB) {
        if (playerA.equals(playerB)) {
            throw new RuntimeException("Players must be different");
        }
        this.playerA = playerA;
        this.playerB = playerB;
        this.pointsHistory = "";
        this.startTime = new Timestamp(System.currentTimeMillis());
    }

    public Match(Player playerA, Player playerB, String pointsHistory) {
        this(playerA, playerB);
        this.pointsHistory = pointsHistory;
        this.isFinished();
        if (!this.isFinished()) {
            throw new RuntimeException("Match must be finished to create it with final score");
        }
    }

    public Match(Player playerA, Player playerB, Integer finalScoreA, Integer finalScoreB) {
        this(playerA, playerB);
        this.finalScoreA = finalScoreA;
        this.finalScoreB = finalScoreB;
        if (!this.isFinished()) {
            throw new RuntimeException("Match must be finished to create it with final score");
        }
    }


    @Override
    public String toString() {
        return "Match{" + "id=" + id + ", pointsHistory='" + pointsHistory + '\'' + ", finalScoreA=" + finalScoreA + ", " +
                "finalScoreB=" + finalScoreB + ", startTime=" + startTime + ", endTime=" + endTime + ", playerA=" +
                playerA + ", playerB=" + playerB + '}';
    }

    /**
     * Gets the number of points a player has scored in a match.
     * <p>
     * The number of points is determined by the points history.
     * If the points history is empty, 0 is returned.
     * <p>
     * The points history is a string of the following format: "A0L;B0R;A1R;B1R;..." where A and B are
     * the players in the match, and the number is the number of points the player has scored, and R or L the
     * service side.
     *
     * @param player the player to get the points for
     * @return the number of points the player has scored
     */
    private Integer getPlayerScore(Player player) {
        PlayerLetter playerLetter = Match.PlayerLetter.valueOf(player.equals(playerA) ? "A" : "B");
        Integer finalScore = playerLetter == Match.PlayerLetter.A ? getFinalScoreA() : getFinalScoreB();
        if (finalScore != null) {
            return finalScore;
        }
        if (Objects.equals(pointsHistory, "")) {
            return 0;
        }
        String[] points = pointsHistory.split(";");

        return Arrays.stream(points)
                .filter(point -> point.startsWith(String.valueOf(playerLetter)))
                .reduce((first, second) -> second)
                .map(point -> point.replaceAll("([A-Z])", ""))
                .map(Integer::valueOf)
                .orElse(0);
    }

    /**
     * Checks if the given player is allowed to serve from the given service side.
     * <p>
     * The rule is that the service side must be different from the service side of the last point
     * and the player must be different from the player who served the last point.
     * If the points history is empty, true is returned.
     * <p>
     * The points history is a string of the following format: "A0L;B0R;A1R;B1R;..." where A and B are
     * the players in the match, and the number is the number of points the player has scored, and R or L the
     * service side.
     *
     * @param player     the player to check
     * @param serviceSide the service side to check
     * @return true if the player is allowed to serve from the service side, false otherwise
     */
    private Boolean isServiceSideCorrect(Player player, ServiceSide serviceSide) {
        if (pointsHistory.isEmpty()) {
            return true;
        }
        String[] points = pointsHistory.split(";");
        PlayerLetter PlayerLetter = Match.PlayerLetter.valueOf(player.equals(playerA) ? "A" : "B");

        return !Arrays.stream(points)
                .reduce((first, second) -> second)
                .map(point -> point.charAt(point.length() - 1))
                .map(String::valueOf)
                .get()
                .equals(String.valueOf(serviceSide))
                || !Arrays.stream(points)
                .reduce((first, second) -> second)
                .map(point -> point.charAt(0))
                .map(String::valueOf)
                .get()
                .equals(String.valueOf(PlayerLetter));
    }

    /**
     * Add a point to a player in the match.
     * <p>
     * The player must be one of the two players in the match. If the player is not in the match,
     * a RuntimeException is thrown.
     * <p>
     * The points history is updated with the new point. If the points history is null, the
     * start time is set to the current time.
     * <p>
     * The points history is a string of the following format: "A0L;B0R;A1R;B1R;..." where A and B are
     * the players in the match, and the number is the number of points the player has scored, and R or L the
     * service side.
     *
     * @param player      the player to add the point to
     * @param serviceSide the side the point was scored on
     */
    public void addService(Player player, String serviceSide) {
        if (player != playerA && player != playerB) {
            throw new RuntimeException("Player not in the party");
        }

        if (isFinished()) {
            throw new RuntimeException("Match is already finished");
        }

        if (!isServiceSideCorrect(player, ServiceSide.valueOf(serviceSide))) {
            throw new RuntimeException("Invalid service side");
        }

        PlayerLetter PlayerLetter = Match.PlayerLetter.valueOf(player.equals(playerA) ? "A" : "B");

        if (Objects.equals(pointsHistory, "")) {
            pointsHistory += PlayerLetter + "0" + serviceSide + ";";
            return;
        }

        int newPlayerScore = getPlayerScore(player) + 1;

        pointsHistory += String.valueOf(PlayerLetter) + newPlayerScore + serviceSide + ";";
    }

    /**
     * Determines if the match is finished.
     * <p>
     * A match is finished when the absolute difference between the two players
     * is 2 or more and at least one of the players has scored 11 points.
     * If the match has already been finished, this method will return true as well.
     *
     * @return true if the match is finished, false otherwise
     */
    public Boolean isFinished() {
        if (Math.abs(getPlayerScore(playerA) - getPlayerScore(playerB)) >= 2 && (getPlayerScore(playerA) >= 11 || getPlayerScore(playerB) >= 11)) {
            finalScoreA = getPlayerScore(playerA);
            finalScoreB = getPlayerScore(playerB);
            endTime = new Timestamp(System.currentTimeMillis());
            return true;
        }
        return false;
    }

    private enum PlayerLetter {
        A, B
    }
}
