package com.leogouchon.hubscore.type;

import com.leogouchon.hubscore.model.Players;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PlayerRank {
    private Players player;
    private int wins;
    private int losses;
    private int totalPointsScored;
    private int totalPointsReceived;

    public PlayerRank(Players player, int wins, int losses, int totalPointsScored, int totalPointsReceived) {
        this.player = player;
        this.wins = wins;
        this.losses = losses;
        this.totalPointsScored = totalPointsScored;
        this.totalPointsReceived = totalPointsReceived;
    }

    public PlayerRank() {
    }
}
