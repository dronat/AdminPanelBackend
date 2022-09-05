package com.woop.Squad4J.listener.logparser;

import com.woop.Squad4J.event.logparser.PlayerConnectedEvent;
import com.woop.Squad4J.listener.GloballyAttachableListener;

@FunctionalInterface
public interface PlayerConnectedListener extends GloballyAttachableListener {
    public void onPlayerConnected(PlayerConnectedEvent playerConnectedEvent);
}
