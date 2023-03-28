package com.woop.Squad4J.listener.logparser;

import com.woop.Squad4J.event.logparser.LeftFromAdminCam;
import com.woop.Squad4J.listener.GloballyAttachableListener;

@FunctionalInterface
public interface PlayerUnPossessListener extends GloballyAttachableListener {
    public void onPlayerUnPossess(LeftFromAdminCam leftFromAdminCam);
}
