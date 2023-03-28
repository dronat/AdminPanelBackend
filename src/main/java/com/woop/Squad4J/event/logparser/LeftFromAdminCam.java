package com.woop.Squad4J.event.logparser;

import com.woop.Squad4J.event.Event;
import com.woop.Squad4J.event.EventType;
import com.woop.Squad4J.listener.logparser.PlayerUnPossessListener;
import lombok.Getter;
import lombok.ToString;

import java.util.Date;

/**
 * Describes an event where a player unposssesses an entity.
 * <p>
 * Note: The chain ID of a {@link LeftFromAdminCam} will match its corresponding {@link EnteredInAdminCam}.
 *
 * @author Robert Engle
 * @see EnteredInAdminCam
 * @see PlayerUnPossessListener
 */
@Getter
@ToString
public class LeftFromAdminCam extends Event {
    private final String playerName;

    /**
     * Constructs a {@link LeftFromAdminCam}.
     *
     * @param date       a {@link Date} corresponding to when this event occurred
     * @param type       the corresponding {@link EventType} for this event
     * @param chainID    the chain ID of this event
     * @param playerName the name of the player who unpossessed the entity
     */
    public LeftFromAdminCam(Date date, EventType type, Integer chainID, String playerName) {
        super(date, type, chainID);
        this.playerName = playerName;
        //TODO: Remove stored chain ID from somewhere
    }
}
