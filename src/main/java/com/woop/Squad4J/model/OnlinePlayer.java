package com.woop.Squad4J.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Objects;

@Getter@Setter
@Accessors(chain = true)
public class OnlinePlayer implements Serializable {
    private final Integer id;
    private final Long steamId;
    private String name;
    private final Integer teamId;
    private final Integer squadID;
    private final Boolean isSquadLeader;
    private final String role;

    public OnlinePlayer(Integer id, Long steamId, String name, Integer teamId, Integer squadID, Boolean isSquadLeader, String role) {
        this.id = id;
        this.steamId = steamId;
        this.name = name;
        this.teamId = teamId;
        this.squadID = squadID;
        this.isSquadLeader = isSquadLeader;
        this.role = role;
    }

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
