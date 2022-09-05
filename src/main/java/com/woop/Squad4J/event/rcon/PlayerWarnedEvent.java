package com.woop.Squad4J.event.rcon;

import com.woop.Squad4J.listener.rcon.PlayerWarnedListener;
import com.woop.Squad4J.event.Event;
import com.woop.Squad4J.event.EventType;
import lombok.Getter;
import lombok.ToString;

import java.util.Date;

/**
 * Describes an event where a player is warned.
 *
 * @see PlayerWarnedListener
 *
 * @author Robert Engle
 */
@Getter
@ToString
public class PlayerWarnedEvent extends Event {
    private final String playerName;
    private final String message;

    /**
     * Constructs a {@link PlayerWarnedEvent}
     *
     * @param date a {@link Date} corresponding to when this event occurred
     * @param type the corresponding {@link EventType} for this event
     * @param playerName the name of the player that was warned
     * @param message the message content of the warning
     */
    public PlayerWarnedEvent(Date date, EventType type, String playerName, String message){
        super(date, type);
        this.playerName = playerName;
        this.message = message;
    }
}
