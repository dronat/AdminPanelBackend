package com.woop.Squad4J.event.logparser;

import com.woop.Squad4J.event.Event;
import com.woop.Squad4J.event.EventType;
import com.woop.Squad4J.listener.logparser.PlayerPossessListener;
import lombok.Getter;
import lombok.ToString;

import java.util.Date;

/**
 * Describes an event where a player possesses an entity.
 *
 * @author Robert Engle
 * @see PlayerPossessListener
 */
@Getter
@ToString
public class EnteredInAdminCam extends Event {
    private final String playerName;
    private final String entityPossessName;

    /**
     * Constructs a {@link EnteredInAdminCam}.
     *
     * @param date        a {@link Date} corresponding to when this event occurred
     * @param type        the corresponding {@link EventType} for this event
     * @param chainID     the chain ID of this event
     * @param playerName  the name of the player
     * @param possessName the name of the entity possessed
     */
    public EnteredInAdminCam(Date date, EventType type, Integer chainID, String playerName, String possessName) {
        super(date, type, chainID);
        this.playerName = playerName;
        this.entityPossessName = possessName;
        //TODO: Store chain ID somewhere
    }
}
