package com.woop.Squad4J.event.rcon;

import com.woop.Squad4J.event.Event;
import com.woop.Squad4J.event.EventType;
import com.woop.Squad4J.listener.rcon.PlayerBannedListener;
import lombok.Getter;
import lombok.ToString;

import java.util.Date;

/**
 * Describes an event where a player is banned. This information is sent from the RCON console, not the squad server log
 * file.
 *
 * @author Robert Engle
 * @see PlayerBannedListener
 */
@Getter
@ToString
public class PlayerBannedEvent extends Event {
    private final long playerId;
    private final long steamId;
    private final String playerName;
    private final String bannedUntil;

    /**
     * Constructs a {@link PlayerBannedEvent}.
     *
     * @param date        a {@link Date} corresponding to when this event occurred
     * @param type        the corresponding {@link EventType} for this event
     * @param playerId    the ID of the player banned. This is NOT the steam64id of the player.
     * @param steamId     the steam64id of the player banned.
     * @param playerName  the name of the player banned
     * @param bannedUntil unix epoch time of when the player is unbanned
     */
    public PlayerBannedEvent(Date date, EventType type, long playerId, long steamId, String playerName, String bannedUntil) {
        super(date, type);
        this.playerId = playerId;
        this.steamId = steamId;
        this.playerName = playerName;
        this.bannedUntil = bannedUntil;
    }
}
