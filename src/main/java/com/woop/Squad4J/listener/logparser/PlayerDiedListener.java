package com.woop.Squad4J.listener.logparser;

import com.woop.Squad4J.event.logparser.PlayerDiedEvent;
import com.woop.Squad4J.listener.GloballyAttachableListener;

@FunctionalInterface
public interface PlayerDiedListener extends GloballyAttachableListener {
    public void onPlayerDied(PlayerDiedEvent playerDiedEvent);
}
