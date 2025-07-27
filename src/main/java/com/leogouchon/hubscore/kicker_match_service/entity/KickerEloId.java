package com.leogouchon.hubscore.kicker_match_service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
@Embeddable
public class KickerEloId implements Serializable {
    @Column(name = "match_id")
    private UUID matchId;

    @Column(name = "player_id")
    private UUID playerId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (getClass() != o.getClass()) return false;

        KickerEloId that = (KickerEloId) o;
        return Objects.equals(matchId, that.matchId) &&
                Objects.equals(playerId, that.playerId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(matchId, playerId);
    }
}
