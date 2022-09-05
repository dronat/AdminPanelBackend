package com.woop.Squad4J.listener.logparser;

import com.woop.Squad4J.event.logparser.PlayerRevivedEvent;
import com.woop.Squad4J.listener.GloballyAttachableListener;

@FunctionalInterface
public interface PlayerRevivedListener extends GloballyAttachableListener {
    public void onPlayerRevived(PlayerRevivedEvent playerRevivedEvent);
}
