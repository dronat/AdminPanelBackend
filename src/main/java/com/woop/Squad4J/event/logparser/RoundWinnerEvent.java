package com.woop.Squad4J.event.logparser;

import com.woop.Squad4J.event.Event;
import com.woop.Squad4J.event.EventType;
import com.woop.Squad4J.listener.logparser.RoundWinnerListener;
import lombok.Getter;
import lombok.ToString;

import java.util.Date;

/**
 * Describes an event where a game is won by a team. The firing of this event implies that a match has ended.
 *
 * @author Robert Engle
 * @see RoundWinnerListener
 */
@Getter
@ToString
public class RoundWinnerEvent extends Event {
    private final String winningFaction;
    private final String layerName;

    /**
     * Constructs a {@link RoundWinnerEvent}.
     *
     * @param date           a {@link Date} corresponding to when this event occurred
     * @param type           the corresponding {@link EventType} for this event
     * @param chainID        the chain ID of this event
     * @param winningFaction the name of the winning faction
     * @param layerName      the name of the current layer
     */
    public RoundWinnerEvent(Date date, EventType type, Integer chainID, String winningFaction, String layerName) {
        super(date, type, chainID);
        this.winningFaction = winningFaction;
        this.layerName = layerName;
    }
}
