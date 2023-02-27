package com.woop.Squad4J.event.rcon;

import com.woop.Squad4J.dto.rcon.Squad;
import com.woop.Squad4J.dto.rcon.Team;
import com.woop.Squad4J.event.Event;
import com.woop.Squad4J.event.EventType;
import com.woop.Squad4J.listener.rcon.SquadListUpdatedListener;
import com.woop.Squad4J.server.RconUpdater;
import lombok.Getter;
import lombok.ToString;

import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Describes an event where a squad list is updated from {@link RconUpdater}.
 *
 * @author Robert Engle
 * @see RconUpdater
 * @see SquadListUpdatedListener
 */
@Getter
@ToString
public class SquadAndTeamListsUpdatedEvent extends Event {
    private final List<Squad> squadList;
    private final List<Team> teamsList;

    /**
     * Constructs a {@link SquadAndTeamListsUpdatedEvent}
     *
     * @param date      a {@link Date} corresponding to when this event occurred
     * @param type      the corresponding {@link EventType} for this event
     * @param squadList a {@link List} representing the squads
     */
    public SquadAndTeamListsUpdatedEvent(Date date, EventType type, List<Squad> squadList, List<Team> teamsList) {
        super(date, type);
        this.squadList = squadList;
        this.teamsList = teamsList;
    }

    /**
     * Gets an unmodifiable {@link List} representing the squads
     *
     * @return a {@link List} representing the squads
     */
    public List<Squad> getSquadList() {
        return Collections.unmodifiableList(squadList);
    }
}
