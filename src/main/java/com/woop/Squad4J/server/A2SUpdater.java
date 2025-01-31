package com.woop.Squad4J.server;

import com.woop.Squad4J.a2s.Query;
import com.woop.Squad4J.a2s.response.A2SCombinedResponse;
import com.woop.Squad4J.concurrent.GlobalThreadPool;
import com.woop.Squad4J.event.Event;
import com.woop.Squad4J.event.EventType;
import com.woop.Squad4J.event.a2s.A2SUpdatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class A2SUpdater {
    private static final Logger LOGGER = LoggerFactory.getLogger(A2SUpdater.class);

    private static boolean initialized = false;

    private A2SUpdater() {
        throw new IllegalStateException("This class cannot be instantiated.");
    }

    public static void init() {
        if (initialized)
            throw new IllegalStateException(A2SUpdater.class.getSimpleName() + " has already been initialized.");

        GlobalThreadPool.getScheduler().scheduleWithFixedDelay(A2SUpdater::updateA2S, 5, 2, TimeUnit.SECONDS);

        initialized = true;

        LOGGER.info("Query service initialized.");
    }

    protected static void updateA2S() {
        LOGGER.trace("Retrieving A2S info");
        A2SCombinedResponse response = Query.queryBoth();

        Event event = new A2SUpdatedEvent(new Date(), EventType.A2S_UPDATED, response);

        LOGGER.trace("A2S info updated");

        EventEmitter.emit(event);
    }
}
