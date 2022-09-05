package com.woop.Squad4J.listener.logparser;

import com.woop.Squad4J.event.logparser.AdminBroadcastEvent;
import com.woop.Squad4J.listener.GloballyAttachableListener;

@FunctionalInterface
public interface AdminBroadcastListener extends GloballyAttachableListener {
    public void onAdminBroadcast(AdminBroadcastEvent adminBroadcastEvent);
}
