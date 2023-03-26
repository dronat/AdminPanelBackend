package com.woop.Squad4J.listener.rcon;

import com.woop.Squad4J.event.rcon.EnteredInAdminCameraEvent;
import com.woop.Squad4J.listener.GloballyAttachableListener;

public interface EnteredInAdminCameraListener extends GloballyAttachableListener {
    public void onEnteredInAdminCamera(EnteredInAdminCameraEvent enteredInAdminCameraEvent);
}
