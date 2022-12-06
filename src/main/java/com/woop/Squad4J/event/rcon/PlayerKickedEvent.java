package com.woop.Squad4J.event.rcon;

import com.woop.Squad4J.event.Event;
import com.woop.Squad4J.event.EventType;
import com.woop.Squad4J.listener.rcon.PlayerKickedListener;
import lombok.Getter;
import lombok.ToString;

import java.util.Date;

/**
 * Describes an event where a player is kicked from the server.
 *
 * @see PlayerKickedListener
 *
 * @author Robert Engle
 */
@Getter
@ToString
public class PlayerKickedEvent extends Event {
    private final long playerId;
    private final long steamId;
    private final String name;

    /**
     * Constructs a {@link PlayerKickedEvent}.
     *
     * @param date a {@link Date} corresponding to when this event occurred
     * @param type the corresponding {@link EventType} for this event
     * @param playerId the id the player kicked. This is NOT the steam64id
     * @param steamId the steam64id of the player kicked
     * @param name the name of the player kicked
     */
    public PlayerKickedEvent(Date date, EventType type, long playerId, long steamId, String name){
        super(date, type);
        this.playerId = playerId;
        this.steamId = steamId;
        this.name = name;
    }
}
