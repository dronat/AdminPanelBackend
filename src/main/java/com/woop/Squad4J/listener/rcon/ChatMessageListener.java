package com.woop.Squad4J.listener.rcon;

import com.woop.Squad4J.event.rcon.ChatMessageEvent;
import com.woop.Squad4J.listener.GloballyAttachableListener;

public interface ChatMessageListener extends GloballyAttachableListener {
    public void onChatMessage(ChatMessageEvent chatMessageEvent);
}
