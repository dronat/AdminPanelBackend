package com.woop.Squad4J.listener.rcon;

import com.woop.Squad4J.event.rcon.PlayerBannedEvent;
import com.woop.Squad4J.listener.GloballyAttachableListener;

public interface PlayerBannedListener extends GloballyAttachableListener {
    public void onPlayerBanned(PlayerBannedEvent playerBannedEvent);
}
