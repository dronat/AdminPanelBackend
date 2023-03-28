package com.woop.Squad4J.listener.logparser;

import com.woop.Squad4J.event.logparser.EnteredInAdminCam;
import com.woop.Squad4J.listener.GloballyAttachableListener;

@FunctionalInterface
public interface PlayerPossessListener extends GloballyAttachableListener {
    public void onPlayerPossess(EnteredInAdminCam enteredInAdminCam);
}
