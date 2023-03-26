package com.woop.Squad4J.dto.rcon;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Objects;

@Data
@Accessors(chain = true)
public class OnlinePlayer implements Serializable {
    private final Integer id;
    private final Long steamId;
    private final String name;
    private final Integer teamId;
    private final Integer squadID;
    private final Boolean isSquadLeader;
    private final String role;
    private Boolean isOnControl = false;
    private Boolean isAdmin = false;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OnlinePlayer onlinePlayer = (OnlinePlayer) o;
        return getSteamId().equals(onlinePlayer.getSteamId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getSteamId());
    }
}
