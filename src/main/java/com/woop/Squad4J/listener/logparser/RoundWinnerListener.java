package com.woop.Squad4J.listener.logparser;

import com.woop.Squad4J.event.logparser.RoundWinnerEvent;
import com.woop.Squad4J.listener.GloballyAttachableListener;

@FunctionalInterface
public interface RoundWinnerListener extends GloballyAttachableListener {
    public void onRoundWinner(RoundWinnerEvent roundWinnerEvent);
}
