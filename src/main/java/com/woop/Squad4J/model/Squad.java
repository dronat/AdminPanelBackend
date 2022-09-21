package com.woop.Squad4J.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
public class Squad implements Serializable {
    private final Integer teamId;
    private final Integer id;
    private String name;
    private final Integer size;
    private final Boolean isLocked;
    private final String creatorName;
    private final String creatorSteam64id;
    private List<OnlinePlayer> players = new ArrayList<>();

    public Squad(Integer teamId, Integer id, String name, Integer size, Boolean isLocked, String creatorName, String creatorSteam64id) {
        this.teamId = teamId;
        this.id = id;
        this.name = name;
        this.size = size;
        this.isLocked = isLocked;
        this.creatorName = creatorName;
        this.creatorSteam64id = creatorSteam64id;
    }

    public Squad addPlayer(OnlinePlayer onlinePlayer) {
        if (players == null) {
            players = new ArrayList<>();
        }
        players.add(onlinePlayer);
        return this;
    }
}
