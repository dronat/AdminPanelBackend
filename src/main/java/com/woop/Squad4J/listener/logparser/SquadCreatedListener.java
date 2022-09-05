package com.woop.Squad4J.listener.logparser;

import com.woop.Squad4J.event.logparser.SquadCreatedEvent;
import com.woop.Squad4J.listener.GloballyAttachableListener;

@FunctionalInterface
public interface SquadCreatedListener extends GloballyAttachableListener {
    public void onSquadCreated(SquadCreatedEvent squadCreatedEvent);
}
