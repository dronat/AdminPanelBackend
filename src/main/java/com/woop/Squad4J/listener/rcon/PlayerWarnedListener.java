package com.woop.Squad4J.listener.rcon;

import com.woop.Squad4J.event.rcon.PlayerWarnedEvent;
import com.woop.Squad4J.listener.GloballyAttachableListener;

public interface PlayerWarnedListener extends GloballyAttachableListener {
    public void onPlayerWarned(PlayerWarnedEvent playerWarnedEvent);
}
