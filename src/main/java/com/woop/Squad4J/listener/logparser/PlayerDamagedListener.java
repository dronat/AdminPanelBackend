package com.woop.Squad4J.listener.logparser;

import com.woop.Squad4J.event.logparser.PlayerDamagedEvent;
import com.woop.Squad4J.listener.GloballyAttachableListener;

@FunctionalInterface
public interface PlayerDamagedListener extends GloballyAttachableListener {
    public void onPlayerDamaged(PlayerDamagedEvent playerDamagedEvent);
}
