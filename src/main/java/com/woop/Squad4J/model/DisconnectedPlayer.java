package com.woop.Squad4J.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Objects;

@Data
@Accessors(chain = true)
public class DisconnectedPlayer implements Serializable  {
    private final Integer id;
    private final long steamId;
    private final String sinceDisconnected;
    private final String name;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DisconnectedPlayer onlinePlayer = (DisconnectedPlayer) o;
        return getSteamId() == onlinePlayer.getSteamId();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getSteamId());
    }
}
