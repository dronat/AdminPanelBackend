package com.woop.Squad4J.listener.logparser;

import com.woop.Squad4J.event.logparser.NewGameEvent;
import com.woop.Squad4J.listener.GloballyAttachableListener;

@FunctionalInterface
public interface NewGameListener extends GloballyAttachableListener {
    public void onNewGame(NewGameEvent newGameEvent);
}
