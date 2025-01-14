package com.woop.Squad4J.event.rcon;

import com.woop.Squad4J.event.Event;
import com.woop.Squad4J.event.EventType;
import com.woop.Squad4J.listener.rcon.EnteredInAdminCameraListener;
import lombok.Getter;
import lombok.ToString;

import java.util.Date;

/**
 * Describes an event where an admin possesses the admin cam.
 *
 * @author Robert Engle
 * @see EnteredInAdminCameraListener
 */
@Getter
@ToString
public class EnteredInAdminCameraEvent extends Event {
    private final long steamId;
    private final String name;

    /**
     * Constructs a {@link EnteredInAdminCameraEvent}.
     *
     * @param date    a {@link Date} corresponding to when this event occurred
     * @param type    the corresponding {@link EventType} for this event
     * @param steamId the steam64id of the admin entering admin cam
     * @param name    the name of the admin entering admin cam
     */
    public EnteredInAdminCameraEvent(Date date, EventType type, long steamId, String name) {
        super(date, type);
        this.steamId = steamId;
        this.name = name;
    }
}
