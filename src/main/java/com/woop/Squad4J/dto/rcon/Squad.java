package com.woop.Squad4J.dto.rcon;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
@Accessors(chain = true)
public class Squad implements Serializable {
    private final Integer teamId;
    private final Integer id;
    private final String name;
    private final Integer size;
    private final Boolean isLocked;
    private final String creatorName;
    private final String creatorSteam64id;
    private List<OnlinePlayer> players = new ArrayList<>();

    public Squad addPlayer(OnlinePlayer onlinePlayer) {
        if (players == null) {
            players = new ArrayList<>();
        }
        players.add(onlinePlayer);
        return this;
    }
}
