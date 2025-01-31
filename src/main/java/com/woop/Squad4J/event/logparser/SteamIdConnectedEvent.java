package com.woop.Squad4J.event.logparser;

import com.woop.Squad4J.event.Event;
import com.woop.Squad4J.event.EventType;
import com.woop.Squad4J.listener.logparser.SteamidConnectedListener;
import lombok.Getter;
import lombok.ToString;

import java.util.Date;

/**
 * Described an event where a player connects to a server an the steam64id is provided. This event is fired
 * BEFORE the corresponding {@link PlayerConnectedEvent}, in which only the player name is provided.
 *
 * @author Robert Engle
 * @see PlayerConnectedEvent
 * @see SteamidConnectedListener
 */
@Getter
@ToString
public class SteamIdConnectedEvent extends Event {
    private final long steamId;
    private final String name;

    /**
     * Constructs a {@link SteamIdConnectedEvent}.
     *
     * @param date    a {@link Date} corresponding to when this event occurred
     * @param type    the corresponding {@link EventType} for this event
     * @param chainID the chain ID of this event
     * @param steamId the steam64id of the player that connected
     * @param name    the name of the player that connected
     */
    public SteamIdConnectedEvent(Date date, EventType type, Integer chainID, long steamId, String name) {
        super(date, type, chainID);
        this.steamId = steamId;
        this.name = name;
        //TODO: Cache steamid with playername for connected, remove from cache when disconnected
    }
}
