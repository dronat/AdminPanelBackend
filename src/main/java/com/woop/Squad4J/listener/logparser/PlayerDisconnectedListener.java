package com.woop.Squad4J.listener.logparser;

import com.woop.Squad4J.event.logparser.PlayerDisconnectedEvent;
import com.woop.Squad4J.listener.GloballyAttachableListener;

@FunctionalInterface
public interface PlayerDisconnectedListener extends GloballyAttachableListener {
    public void onPlayerDisconnected(PlayerDisconnectedEvent playerDisconnectedEvent);
}
