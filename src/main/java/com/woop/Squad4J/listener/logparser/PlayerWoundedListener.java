package com.woop.Squad4J.listener.logparser;

import com.woop.Squad4J.event.logparser.PlayerWoundedEvent;
import com.woop.Squad4J.listener.GloballyAttachableListener;

@FunctionalInterface
public interface PlayerWoundedListener extends GloballyAttachableListener {
    public void onPlayerWoundedEvent(PlayerWoundedEvent playerWoundedEvent);
}
