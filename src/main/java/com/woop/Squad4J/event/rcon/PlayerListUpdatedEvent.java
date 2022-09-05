package com.woop.Squad4J.event.rcon;

import com.woop.Squad4J.listener.rcon.PlayerListUpdatedListener;
import com.woop.Squad4J.model.DisconnectedPlayer;
import com.woop.Squad4J.model.OnlinePlayer;
import com.woop.Squad4J.event.Event;
import com.woop.Squad4J.event.EventType;
import com.woop.Squad4J.server.RconUpdater;
import lombok.Getter;
import lombok.ToString;

import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Describes an event where a player list is updated from {@link RconUpdater}.
 *
 * @see RconUpdater
 * @see PlayerListUpdatedListener
 *
 * @author Robert Engle
 */
@Getter
@ToString
public class PlayerListUpdatedEvent extends Event {
    private final List<OnlinePlayer> onlinePlayersList;
    private final List<DisconnectedPlayer> disconnectedPlayersList;

    /**
     * Constructs a {@link PlayerListUpdatedEvent}.
     *
     * @param date a {@link Date} corresponding to when this event occurred
     * @param type the corresponding {@link EventType} for this event
     * @param onlinePlayersList a {@link List} containing players in the server
     */
    public PlayerListUpdatedEvent(Date date, EventType type, List<OnlinePlayer> onlinePlayersList, List<DisconnectedPlayer> disconnectedPlayersList) {
        super(date, type);
        this.onlinePlayersList = onlinePlayersList;
        this.disconnectedPlayersList = disconnectedPlayersList;
    }

    /**
     * Returns an unmodifiable {@link List} of the online players.
     *
     * @return an unmodifiable list of the online players
     */
    public List<OnlinePlayer> getOnlinePlayersList(){
        return Collections.unmodifiableList(onlinePlayersList);
    }

    /**
     * Returns an unmodifiable {@link List} of the disconnected players.
     *
     * @return an unmodifiable list of the disconnected players
     */
    public List<DisconnectedPlayer> getDisconnectedPlayersList(){
        return Collections.unmodifiableList(disconnectedPlayersList);
    }
}
