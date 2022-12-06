package com.woop.Squad4J.event.rcon;

import com.woop.Squad4J.event.Event;
import com.woop.Squad4J.event.EventType;
import com.woop.Squad4J.listener.rcon.ChatMessageListener;
import lombok.Getter;
import lombok.ToString;

import java.sql.Timestamp;
import java.util.Date;

/**
 * Describes an event where a chat message is sent by a player.
 *
 * @see ChatMessageListener
 *
 * @author Robert Engle
 */
@Getter
@ToString
public class ChatMessageEvent extends Event {
    private final String chatType;
    private final long steamId;
    private final String playerName;
    private final String message;
    private final String completeString;
    private final Timestamp time;

    /**
     * Constructs a {@link ChatMessageEvent}.
     *
     * @param date a {@link Date} corresponding to when this event occurred
     * @param type the corresponding {@link EventType} for this event
     * @param chatType the chat type of the event. Will be one of: <code>ChatAll</code>, <code>ChatTeam</code>,
     *                 <code>ChatSquad</code>, or <code>ChatAdmin</code>
     * @param steamId the steam64id of the player sending the chat message
     * @param playerName the name of the player sending the chat message
     * @param message the content of the message sent
     */
    public ChatMessageEvent(Date date, EventType type, String chatType, long steamId, String playerName, String message){
        super(date, type);
        this.chatType = chatType;
        this.steamId = steamId;
        this.playerName = playerName;
        this.message = message;
        time = new Timestamp(System.currentTimeMillis());
        completeString = String.format(time + " [%s] %s: %s", chatType, playerName, message);;
    }
}
