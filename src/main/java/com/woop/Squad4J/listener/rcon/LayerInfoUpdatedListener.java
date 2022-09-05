package com.woop.Squad4J.listener.rcon;

import com.woop.Squad4J.event.rcon.LayerInfoUpdatedEvent;
import com.woop.Squad4J.listener.GloballyAttachableListener;

public interface LayerInfoUpdatedListener extends GloballyAttachableListener {
    public void onLayerInfoUpdated(LayerInfoUpdatedEvent layerInfoUpdatedEvent);
}
