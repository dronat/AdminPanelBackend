package com.woop.Squad4J.listener.rcon;

import com.woop.Squad4J.event.rcon.SquadAndTeamListsUpdatedEvent;
import com.woop.Squad4J.listener.GloballyAttachableListener;

public interface SquadListUpdatedListener extends GloballyAttachableListener {
    public void onSquadListUpdated(SquadAndTeamListsUpdatedEvent squadAndTeamListsUpdatedEvent);
}
