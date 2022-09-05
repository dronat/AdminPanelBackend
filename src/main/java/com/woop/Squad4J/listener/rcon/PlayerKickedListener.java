package com.woop.Squad4J.listener.rcon;

import com.woop.Squad4J.event.rcon.PlayerKickedEvent;
import com.woop.Squad4J.listener.GloballyAttachableListener;

public interface PlayerKickedListener extends GloballyAttachableListener {
    public void onPlayerKicked(PlayerKickedEvent playerKickedEvent);
}
