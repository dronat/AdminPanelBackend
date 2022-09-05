package com.woop.Squad4J.model;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Objects;

@Data
@Accessors(chain = true)
public class OnlinePlayer implements Serializable {
    private final Integer id;
    private final String steam64id;
    private final String name;
    private final Integer teamId;
    private final Integer squadID;
    private final Boolean isSquadLeader;
    private final String role;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OnlinePlayer onlinePlayer = (OnlinePlayer) o;
        return getSteam64id().equals(onlinePlayer.getSteam64id());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getSteam64id());
    }
}
