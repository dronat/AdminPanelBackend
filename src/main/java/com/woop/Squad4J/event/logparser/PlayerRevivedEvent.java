package com.woop.Squad4J.event.logparser;

import com.woop.Squad4J.event.Event;
import com.woop.Squad4J.event.EventType;
import com.woop.Squad4J.listener.logparser.PlayerRevivedListener;
import lombok.Getter;
import lombok.ToString;

import java.util.Date;

/**
 * Describes an event where a player is revived.
 *
 * @author Robert Engle
 * @see PlayerRevivedListener
 */
@Getter
@ToString
public class PlayerRevivedEvent extends Event {
    private final String reviverName;
    private final String victimName;

    /**
     * Constructs a {@link PlayerRevivedEvent}.
     *
     * @param date        a {@link Date} corresponding to when this event occurred
     * @param type        the corresponding {@link EventType} for this event
     * @param chainID     the chain ID of this event
     * @param reviverName the name of the reviver
     * @param victimName  the name of the player who was revived
     */
    public PlayerRevivedEvent(Date date, EventType type, Integer chainID, String reviverName, String victimName) {
        super(date, type, chainID);
        this.reviverName = reviverName;
        this.victimName = victimName;
    }
}
