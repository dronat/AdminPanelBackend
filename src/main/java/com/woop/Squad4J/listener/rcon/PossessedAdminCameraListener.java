package com.woop.Squad4J.listener.rcon;

import com.woop.Squad4J.event.rcon.PossessedAdminCameraEvent;
import com.woop.Squad4J.listener.GloballyAttachableListener;

public interface PossessedAdminCameraListener extends GloballyAttachableListener {
    public void onPossessedAdminCamera(PossessedAdminCameraEvent possessedAdminCameraEvent);
}
