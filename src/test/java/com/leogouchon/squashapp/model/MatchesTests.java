package com.leogouchon.squashapp.model;

import org.junit.jupiter.api.Test;

public class MatchesTests {
    Players playerA = new Players("John", "Doe");
    Players playerB = new Players("Jane", "Dae");
    Players playerC = new Players("Jack", "Durand");

    @Test
    public void testConstructor() {
        Matches match = new Matches(playerA, playerB);

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
            new Matches(playerA, playerA);
            assert false : "Expected RuntimeException for same player twice.";
        } catch (RuntimeException e) {
            assert e.getMessage().equals("Players must be different");
        }
    }

    @Test
    public void testAddServiceToPlayerA() {
        Matches match = new Matches(playerA, playerB);

        match.addService(playerA, "R");

        assert match.getPointsHistory().equals("A0R;");

        match.addService(playerA, "L");

        assert match.getPointsHistory().equals("A0R;A1L;");
    }

    @Test
    public void testAddServiceToPlayerB() {
        Matches match = new Matches(playerA, playerB);

        match.addService(playerB, "R");

        assert match.getPointsHistory().equals("B0R;");

        match.addService(playerB, "L");

        assert match.getPointsHistory().equals("B0R;B1L;");
    }

    @Test
    public void testAddServiceSameSide() {
        Matches match = new Matches(playerA, playerB);
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
        Matches match = new Matches(playerA, playerB);

        try {
            match.addService(playerC, "L");
            assert false : "Expected RuntimeException for player not in the match.";
        } catch (RuntimeException e) {
            assert e.getMessage().equals("Player not in the party");
        }
    }

    @Test
    public void testIsFinished11to0() {
        Matches match = new Matches(playerA, playerB);
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
        Matches match = new Matches(playerA, playerB);
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
        Matches match = new Matches(playerA, playerB);
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
