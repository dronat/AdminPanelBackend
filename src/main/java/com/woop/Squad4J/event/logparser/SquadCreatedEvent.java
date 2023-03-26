package com.woop.Squad4J.event.logparser;

import com.woop.Squad4J.event.Event;
import com.woop.Squad4J.event.EventType;
import com.woop.Squad4J.listener.logparser.SquadCreatedListener;
import lombok.Getter;
import lombok.ToString;

import java.util.Date;

/**
 * Describes an event where a squad is created by a player.
 *
 * @author Robert Engle
 * @see SquadCreatedListener
 */
@Getter
@ToString
public class SquadCreatedEvent extends Event {
    private final String playerName;
    private final long steamId;
    private final Integer squadid;
    private final String squadName;
    private final String teamName;

    /**
     * Constructs a {@link SquadCreatedEvent}.
     *
     * @param date       a {@link Date} corresponding to when this event occurred
     * @param type       the corresponding {@link EventType} for this event
     * @param chainID    the chain ID of this event
     * @param playerName the name of the player who created the squad
     * @param steamId    the steam64id of the player who created the squad
     * @param squadid    the id of the squad created
     * @param squadName  the name of the squad
     * @param teamName   the name of the team the squad was created on
     */
    public SquadCreatedEvent(Date date, EventType type, Integer chainID, String playerName, long steamId,
                             Integer squadid, String squadName, String teamName) {
        super(date, type, chainID);
        this.playerName = playerName;
        this.steamId = steamId;
        this.squadid = squadid;
        this.squadName = squadName;
        this.teamName = teamName;
    }
}
