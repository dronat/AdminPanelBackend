package com.woop.Squad4J.event.rcon;

import com.woop.Squad4J.event.Event;
import com.woop.Squad4J.event.EventType;
import com.woop.Squad4J.listener.rcon.LeftFromAdminCameraListener;
import lombok.Getter;
import lombok.ToString;

import java.util.Date;

/**
 * Describes an event where an admin unpossesses the admin cam.
 *
 * @see LeftFromAdminCameraListener
 *
 * @author Robert Engle
 */
@Getter
@ToString
public class LeftFromAdminCameraEvent extends Event {
    private final long steamId;
    private final String name;

    /**
     * Constructs a {@link LeftFromAdminCameraEvent}.
     *
     * @param date a {@link Date} corresponding to when this event occurred
     * @param type the corresponding {@link EventType} for this event
     * @param steamId the steam64id of the admin who exited admin cam
     * @param name the name of the admin who exited admin cam
     */
    public LeftFromAdminCameraEvent(Date date, EventType type, long steamId, String name){
        super(date, type);
        this.steamId = steamId;
        this.name = name;
    }
}
