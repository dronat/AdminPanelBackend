package com.woop.Squad4J.event.logparser;

import com.woop.Squad4J.event.Event;
import com.woop.Squad4J.event.EventType;
import com.woop.Squad4J.listener.logparser.ServerTickRateListener;
import lombok.Getter;
import lombok.ToString;

import java.util.Date;

/**
 * Describes an event where the tick rate of the server is updated.
 *
 * @author Robert Engle
 * @see ServerTickRateListener
 */
@Getter
@ToString
public class ServerTickRateEvent extends Event {
    private final Double tickRate;

    /**
     * Constructs a {@link ServerTickRateEvent}.
     *
     * @param date     a {@link Date} corresponding to when this event occurred
     * @param type     the corresponding {@link EventType} for this event
     * @param chainID  the chain ID of this event
     * @param tickRate the reported tick rate for this event
     */
    public ServerTickRateEvent(Date date, EventType type, Integer chainID, Double tickRate) {
        super(date, type, chainID);
        this.tickRate = tickRate;
    }
}
