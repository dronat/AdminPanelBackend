package com.woop.Squad4J.listener.rcon;

import com.woop.Squad4J.event.rcon.LeftFromAdminCameraEvent;
import com.woop.Squad4J.listener.GloballyAttachableListener;

public interface LeftFromAdminCameraListener extends GloballyAttachableListener {
    public void onLeftFromFAdminCamera(LeftFromAdminCameraEvent leftFromAdminCameraEvent);
}
