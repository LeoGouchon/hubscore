package com.leogouchon.squashapp.model;

import org.junit.jupiter.api.Test;

public class MatchTests {
    Player playerA = new Player("John", "Doe");
    Player playerB = new Player("Jane", "Dae");
    Player playerC = new Player("Jack", "Durand");

    @Test
    public void testConstructor() {
        Match match = new Match(playerA, playerB);

        assert match.getPlayerA().equals(playerA);
        assert match.getPlayerB().equals(playerB);
        assert match.getPointsHistory().isEmpty();
        assert match.getFinalScoreA() == null;
        assert match.getFinalScoreB() == null;
        assert match.getStartTime() == null;
        assert match.getEndTime() == null;
    }

    @Test
    public void testConstructorSamePlayerTwice() {
        try {
            new Match(playerA, playerA);
            assert false : "Expected RuntimeException for same player twice.";
        } catch (RuntimeException e) {
            assert e.getMessage().equals("Players must be different");
        }
    }

    @Test
    public void testAddServiceToPlayerA() {
        Match match = new Match(playerA, playerB);

        match.addService(playerA, "R");

        assert match.getPointsHistory().equals("A0R;");

        match.addService(playerA, "L");

        assert match.getPointsHistory().equals("A0R;A1L;");
    }

    @Test
    public void testAddServiceToPlayerB() {
        Match match = new Match(playerA, playerB);

        match.addService(playerB, "R");

        assert match.getPointsHistory().equals("B0R;");

        match.addService(playerB, "L");

        assert match.getPointsHistory().equals("B0R;B1L;");
    }

    @Test
    public void testAddServiceSameSide() {
        Match match = new Match(playerA, playerB);
        match.addService(playerA, "R");

        try {
            match.addService(playerA, "R");
            assert false : "Expected RuntimeException for player already served.";
        } catch (RuntimeException e) {
            assert e.getMessage().equals("Invalid service side");
        }
    }

    @Test
    public void testAddServiceThirdPlayer() {
        Match match = new Match(playerA, playerB);

        try {
            match.addService(playerC, "L");
            assert false : "Expected RuntimeException for player not in the match.";
        } catch (RuntimeException e) {
            assert e.getMessage().equals("Player not in the party");
        }
    }

    @Test
    public void testIsFinished11to0() {
        Match match = new Match(playerA, playerB);
        match.addService(playerA, "R");
        match.addService(playerA, "L");
        match.addService(playerA, "R");
        match.addService(playerA, "L");
        match.addService(playerA, "R");
        match.addService(playerA, "L");
        match.addService(playerA, "R");
        match.addService(playerA, "L");
        match.addService(playerA, "R");
        match.addService(playerA, "L");
        match.addService(playerA, "R");
        match.addService(playerA, "L");

        assert match.getFinalScoreA() == null;
        assert match.getFinalScoreB() == null;

        assert match.isFinished();

        assert match.getFinalScoreA() == 11;
        assert match.getFinalScoreB() == 0;
    }

    @Test
    public void testIsFinished11to13() {
        Match match = new Match(playerA, playerB);
        match.addService(playerA, "R");
        match.addService(playerA, "L");
        match.addService(playerA, "R");
        match.addService(playerA, "L");
        match.addService(playerA, "R");
        match.addService(playerA, "L");
        match.addService(playerA, "R");
        match.addService(playerA, "L");
        match.addService(playerA, "R");
        match.addService(playerA, "L");
        match.addService(playerA, "R");
        match.addService(playerB, "R");
        match.addService(playerB, "L");
        match.addService(playerB, "R");
        match.addService(playerB, "L");
        match.addService(playerB, "R");
        match.addService(playerB, "L");
        match.addService(playerB, "R");
        match.addService(playerB, "L");
        match.addService(playerB, "R");
        match.addService(playerB, "L");
        match.addService(playerB, "R");
        match.addService(playerA, "L");
        match.addService(playerB, "R");
        match.addService(playerB, "L");

        assert match.getFinalScoreA() == null;
        assert match.getFinalScoreB() == null;

        assert match.isFinished();

        assert match.getFinalScoreA() == 11;
        assert match.getFinalScoreB() == 13;
    }

    @Test
    public void testIsFinished11to11() {
        Match match = new Match(playerA, playerB);
        match.addService(playerA, "R");
        match.addService(playerA, "L");
        match.addService(playerA, "R");
        match.addService(playerA, "L");
        match.addService(playerA, "R");
        match.addService(playerA, "L");
        match.addService(playerA, "R");
        match.addService(playerA, "L");
        match.addService(playerA, "R");
        match.addService(playerA, "L");
        match.addService(playerA, "R");
        match.addService(playerB, "R");
        match.addService(playerB, "L");
        match.addService(playerB, "R");
        match.addService(playerB, "L");
        match.addService(playerB, "R");
        match.addService(playerB, "L");
        match.addService(playerB, "R");
        match.addService(playerB, "L");
        match.addService(playerB, "R");
        match.addService(playerB, "L");
        match.addService(playerB, "R");
        match.addService(playerA, "L");

        assert !match.isFinished();

        assert match.getFinalScoreA() == null;
        assert match.getFinalScoreB() == null;
    }
}
