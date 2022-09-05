package com.woop.Squad4J.listener.logparser;

import com.woop.Squad4J.event.logparser.SteamidConnectedEvent;
import com.woop.Squad4J.listener.GloballyAttachableListener;

@FunctionalInterface
public interface SteamidConnectedListener extends GloballyAttachableListener {
    public void onSteamidConnected(SteamidConnectedEvent steamidConnectedEvent);
}
