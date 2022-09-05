package com.woop.Squad4J.listener.logparser;

import com.woop.Squad4J.event.logparser.DeployableDamagedEvent;
import com.woop.Squad4J.listener.GloballyAttachableListener;

@FunctionalInterface
public interface DeployableDamagedListener extends GloballyAttachableListener {
    public void onDeployableDamaged(DeployableDamagedEvent deployableDamagedEvent);
}
