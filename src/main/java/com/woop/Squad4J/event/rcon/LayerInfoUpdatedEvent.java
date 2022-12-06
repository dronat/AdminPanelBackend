package com.woop.Squad4J.event.rcon;

import com.woop.Squad4J.event.Event;
import com.woop.Squad4J.event.EventType;
import com.woop.Squad4J.listener.rcon.LayerInfoUpdatedListener;
import com.woop.Squad4J.server.RconUpdater;
import lombok.Getter;
import lombok.ToString;

import java.util.Date;

/**
 * Describes an event where layer information is updated from {@link RconUpdater}.
 *
 * @see RconUpdater
 * @see LayerInfoUpdatedListener
 *
 * @author Robert Engle
 */
@Getter
@ToString
public class LayerInfoUpdatedEvent extends Event {
    private final String currentMap;
    private final String currentLayer;
    private final String nextMap;
    private final String nextLayer;

    /**
     * Constructs a {@link LayerInfoUpdatedEvent}.
     *
     * @param date a {@link Date} corresponding to when this event occurred
     * @param type the corresponding {@link EventType} for this event
     * @param currentMap the current map being played
     * @param currentLayer the current layer being played
     * @param nextMap the next map
     * @param nextLayer the next layer
     */
    public LayerInfoUpdatedEvent(Date date, EventType type, String currentMap, String currentLayer, String nextMap, String nextLayer) {
        super(date, type);
        this.currentMap = currentMap;
        this.currentLayer = currentLayer;
        this.nextMap = nextMap;
        this.nextLayer = nextLayer;
    }
}
