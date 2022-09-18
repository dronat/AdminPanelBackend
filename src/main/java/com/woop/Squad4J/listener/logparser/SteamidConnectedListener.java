package com.woop.Squad4J.listener.logparser;

import com.woop.Squad4J.event.logparser.SteamIdConnectedEvent;
import com.woop.Squad4J.listener.GloballyAttachableListener;

@FunctionalInterface
public interface SteamidConnectedListener extends GloballyAttachableListener {
    public void onSteamIdConnected(SteamIdConnectedEvent steamidConnectedEvent);
}
