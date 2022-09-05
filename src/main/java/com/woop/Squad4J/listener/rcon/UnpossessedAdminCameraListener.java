package com.woop.Squad4J.listener.rcon;

import com.woop.Squad4J.event.rcon.UnpossessedAdminCameraEvent;
import com.woop.Squad4J.listener.GloballyAttachableListener;

public interface UnpossessedAdminCameraListener extends GloballyAttachableListener {
    public void onUnpossessedAdminCamera(UnpossessedAdminCameraEvent unpossessedAdminCameraEvent);
}
