package com.woop.Squad4J.concurrent;

import java.util.concurrent.*;

/**
 * Class to provide global access to thread pool executors.
 * <p>
 * Some implementation from:
 * <a href="https://github.com/Javacord/Javacord/blob/master/javacord-core/src/main/java/org/javacord/core/util/concurrent/ThreadPoolImpl.java">Javacord ThreadPoolImpl</a>
 */
public class GlobalThreadPool {
    private static final int CORE_POOL_SIZE = 1;
    private static final int MAXIMUM_POOL_SIZE = Integer.MAX_VALUE;
    private static final int KEEP_ALIVE_TIME = 60;
    private static final TimeUnit TIME_UNIT = TimeUnit.SECONDS;

    private static final ExecutorService executorService = new ThreadPoolExecutor(
            CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE_TIME, TIME_UNIT, new SynchronousQueue<>(),
            new NamedThreadFactory("Squad4J - Central ExecutorService"));
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(
            CORE_POOL_SIZE, new NamedThreadFactory("Squad4J - Central ScheduledExecutorService"));

    private GlobalThreadPool() {
        throw new IllegalStateException("Utility classes cannot be instantiated.");
    }

    /**
     * Gets the central {@link ExecutorService} for Squad4J.
     *
     * @return the central {@link ExecutorService} for Squad4J
     */
    public static ExecutorService getExecutorService() {
        return executorService;
    }

    /**
     * Gets the central {@link ScheduledExecutorService} for Squad4J.
     *
     * @return the central {@link ScheduledExecutorService} for Squad4J
     */
    public static ScheduledExecutorService getScheduler() {
        return scheduler;
    }
}
