package com.woop.Squad4J.event.logparser;

import com.woop.Squad4J.event.Event;
import com.woop.Squad4J.event.EventType;
import com.woop.Squad4J.listener.logparser.PlayerDisconnectedListener;
import lombok.Getter;
import lombok.ToString;

import java.util.Date;

/**
 * Describes an event where a player disconnects from the server.
 *
 * @author Robert Engle
 * @see PlayerDisconnectedListener
 */
@Getter
@ToString
public class PlayerDisconnectedEvent extends Event {
    private final long steamId;

    /**
     * Constructs a {@link PlayerDisconnectedEvent}.
     *
     * @param date    a {@link Date} representing when the event occurred
     * @param type    the corresponding {@link EventType} for this event
     * @param chainID the chain ID of this event
     * @param steamId the Steam64ID of the player who disconnected
     */
    public PlayerDisconnectedEvent(Date date, EventType type, Integer chainID, long steamId) {
        super(date, type, chainID);
        this.steamId = steamId;
    }
}
