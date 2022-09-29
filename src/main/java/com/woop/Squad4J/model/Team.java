package com.woop.Squad4J.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
@Accessors(chain = true)
public class Team implements Serializable {
    private final String teamName;
    private final Integer id;
    private List<Squad> squads = new ArrayList<>();
    private List<OnlinePlayer> playersWithoutSquad = new ArrayList<>();

    public Squad getSquadById(Integer id) {
        Squad squadWithPlayers = null;
        for (Squad squad : squads) {
            if (squad.getId().equals(id)) {
                squadWithPlayers = squad;
            }
        }
        return squadWithPlayers;
    }

    public Team addSquad(Squad squad) {
        if (squads == null) {
            squads = new ArrayList<>();
        }
        squads.add(squad);
        return this;
    }

    public Team addPlayerWithoutSquad(OnlinePlayer onlinePlayer) {
        if (playersWithoutSquad == null) {
            playersWithoutSquad = new ArrayList<>();
        }
        playersWithoutSquad.add(onlinePlayer);
        return this;
    }
}
