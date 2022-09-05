package com.woop.Squad4J.listener.logparser;

import com.woop.Squad4J.event.logparser.PlayerPossessEvent;
import com.woop.Squad4J.listener.GloballyAttachableListener;

@FunctionalInterface
public interface PlayerPossessListener extends GloballyAttachableListener {
    public void onPlayerPossess(PlayerPossessEvent playerPossessEvent);
}
