package com.woop.Squad4J.listener.logparser;

import com.woop.Squad4J.event.logparser.ServerTickRateEvent;
import com.woop.Squad4J.listener.GloballyAttachableListener;

@FunctionalInterface
public interface ServerTickRateListener extends GloballyAttachableListener {
    public void onServerTickRate(ServerTickRateEvent serverTickRateEvent);
}
