package com.woop.Squad4J.listener.rcon;

import com.woop.Squad4J.event.rcon.PlayerListUpdatedEvent;
import com.woop.Squad4J.listener.GloballyAttachableListener;

public interface PlayerListUpdatedListener extends GloballyAttachableListener {
    public void onPlayerListUpdated(PlayerListUpdatedEvent playerListUpdatedEvent);
}
