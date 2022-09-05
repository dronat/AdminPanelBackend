package com.woop.Squad4J.plugins;

import com.woop.Squad4J.a2s.response.A2SRulesResponse;
import com.woop.Squad4J.event.a2s.A2SUpdatedEvent;
import com.woop.Squad4J.event.rcon.ChatMessageEvent;
import com.woop.Squad4J.event.rcon.LayerInfoUpdatedEvent;
import com.woop.Squad4J.event.rcon.PossessedAdminCameraEvent;
import com.woop.Squad4J.event.rcon.UnpossessedAdminCameraEvent;
import com.woop.Squad4J.listener.a2s.A2SUpdatedListener;
import com.woop.Squad4J.listener.rcon.ChatMessageListener;
import com.woop.Squad4J.listener.rcon.LayerInfoUpdatedListener;
import com.woop.Squad4J.listener.rcon.PossessedAdminCameraListener;
import com.woop.Squad4J.listener.rcon.UnpossessedAdminCameraListener;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Robert Engle
 *
 * Test class which implements various listeners to test their functionality. Also tests the event binding
 * with these listeners.
 *
 * Will be removed in initial release.
 */
@NoArgsConstructor
public class DevelopmentTestPlugin implements A2SUpdatedListener, LayerInfoUpdatedListener, ChatMessageListener,
        PossessedAdminCameraListener, UnpossessedAdminCameraListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(DevelopmentTestPlugin.class);

    @Override
    public void onA2SUpdated(A2SUpdatedEvent a2SUpdatedEvent) {
        A2SRulesResponse rules = a2SUpdatedEvent.getResponse().getRules();
        rules.getRuleEntrySet().forEach(entry -> {
            LOGGER.debug("{} : {}", entry.getKey(), entry.getValue());
        });
    }

    @Override
    public void onLayerInfoUpdated(LayerInfoUpdatedEvent layerInfoUpdatedEvent) {
        String currentLayer = layerInfoUpdatedEvent.getCurrentLayer();
        String nextLayer = layerInfoUpdatedEvent.getNextLayer();
        LOGGER.debug("Current Layer - {}", currentLayer);
        LOGGER.debug("Next Layer - {}", nextLayer);
    }

    @Override
    public void onChatMessage(ChatMessageEvent chatMessageEvent) {
        String chatType = chatMessageEvent.getChatType();
        String name = chatMessageEvent.getPlayerName();
        String message = chatMessageEvent.getMessage();

        LOGGER.info("New Chat: [{}] {}: {}",chatType, name, message);
    }

    @Override
    public void onPossessedAdminCamera(PossessedAdminCameraEvent possessedAdminCameraEvent) {
        LOGGER.info("User {} entered admin cam.", possessedAdminCameraEvent.getName());
    }

    @Override
    public void onUnpossessedAdminCamera(UnpossessedAdminCameraEvent unpossessedAdminCameraEvent) {
        LOGGER.info("User {} exited admin cam.", unpossessedAdminCameraEvent.getName());
    }
}
