package com.woop.Squad4J.event.a2s;

import com.woop.Squad4J.a2s.response.A2SCombinedResponse;
import com.woop.Squad4J.event.Event;
import com.woop.Squad4J.event.EventType;
import com.woop.Squad4J.listener.a2s.A2SUpdatedListener;
import lombok.Getter;
import lombok.ToString;

import java.util.Date;

/**
 * Describes an event where A2S information is retrieved and updated. Contains a {@link A2SCombinedResponse},
 * which contains the responses for both A2S_INFO and A2S_RULES queries.
 *
 * @author Robert Engle
 * @see A2SUpdatedListener
 */
@Getter
@ToString
public class A2SUpdatedEvent extends Event {
    private final A2SCombinedResponse response;

    /**
     * Constructs a {@link A2SUpdatedEvent}
     *
     * @param date     a {@link Date} representing when the event occurred
     * @param type     the corresponding {@link EventType} for this event
     * @param response the {@link A2SCombinedResponse} containing A2S information
     */
    public A2SUpdatedEvent(Date date, EventType type, A2SCombinedResponse response) {
        super(date, type);
        this.response = response;
    }
}
