package com.woop.Squad4J.event.rcon;

import com.woop.Squad4J.listener.rcon.UnpossessedAdminCameraListener;
import com.woop.Squad4J.event.Event;
import com.woop.Squad4J.event.EventType;
import lombok.Getter;
import lombok.ToString;

import java.util.Date;

/**
 * Describes an event where an admin unpossesses the admin cam.
 *
 * @see UnpossessedAdminCameraListener
 *
 * @author Robert Engle
 */
@Getter
@ToString
public class UnpossessedAdminCameraEvent extends Event {
    private final String steamid;
    private final String name;

    /**
     * Constructs a {@link UnpossessedAdminCameraEvent}.
     *
     * @param date a {@link Date} corresponding to when this event occurred
     * @param type the corresponding {@link EventType} for this event
     * @param steamid the steam64id of the admin who exited admin cam
     * @param name the name of the admin who exited admin cam
     */
    public UnpossessedAdminCameraEvent(Date date, EventType type, String steamid, String name){
        super(date, type);
        this.steamid = steamid;
        this.name = name;
    }
}
