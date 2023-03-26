package com.woop.Squad4J.dto.rcon;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
@Accessors(chain = true)
public class OnlineInfo implements Serializable {
    private List<Team> teams = new ArrayList<>();

    public Team getTeamById(Integer id) {
        Team team = null;
        for (Team tmpTeam : teams) {
            if (tmpTeam.getId().equals(id)) {
                team = tmpTeam;
            }
        }
        return team;
    }

    public OnlineInfo addTeam(Team team) {
        if (teams == null) {
            teams = new ArrayList<>();
        }
        teams.add(team);
        return this;
    }

    /*public OnlineInfo addDisconnectedPlayer(DisconnectedPlayer disconnectedPlayer) {
        if (disconnectedPlayers == null) {
            disconnectedPlayers = new ArrayList<>();
        }
        disconnectedPlayers.add(disconnectedPlayer);
        return this;
    }*/
}
