package com.woop.Squad4J.a2s;

import com.ibasco.agql.protocols.valve.source.query.SourceQueryClient;
import com.ibasco.agql.protocols.valve.source.query.info.SourceQueryInfoResponse;
import com.ibasco.agql.protocols.valve.source.query.players.SourceQueryPlayerResponse;
import com.ibasco.agql.protocols.valve.source.query.rules.SourceQueryRulesResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class NewQueryImpl {
    private final Logger LOGGER = LoggerFactory.getLogger(NewQueryImpl.class);
    private final String HOST;
    private final Integer PORT;
    private final int TIMEOUT;
    private SourceQueryClient queryClient;
    private InetSocketAddress address;

    public NewQueryImpl(String host, Integer port, int timout) {
        PORT = port;
        HOST = host;
        TIMEOUT = timout;

    }

    public NewQueryImpl init() {
        try {
            LOGGER.info("Connecting to Query");
            queryClient = new SourceQueryClient();
            address = new InetSocketAddress(HOST, PORT);
            LOGGER.info("Query connected");
        } catch (Exception e) {
            LOGGER.error("Initialization error", e);
        }
        return this;
    }

    public void disconnect() {
        try {
            queryClient.close();
        } catch (IOException e) {
            LOGGER.error("Failed to close Query connection", e);
            throw new RuntimeException(e);
        }
    }

    public void reconnect() {
        LOGGER.info("Reconnecting to Query");
        disconnect();
        init();
        LOGGER.info("Query reconnected successfully");
    }

    public SourceQueryInfoResponse getQueryInfo() {
        try {
            SourceQueryInfoResponse sqir = null;
            while (sqir == null) {
                try {
                    sqir = queryClient.getInfo(address).get(TIMEOUT, TimeUnit.MILLISECONDS);
                } catch (TimeoutException ignore) {}
                if (sqir == null) {
                    LOGGER.warn("Failed to get Query info, trying again");
                }
            }
            return sqir;
        } catch (Exception e) {
            LOGGER.error("Failed to get Query info", e);
            reconnect();
            return getQueryInfo();
        }
    }

    public SourceQueryRulesResponse getQueryRules() {
        try {
            SourceQueryRulesResponse sqir = null;
            while (sqir == null) {
                try {
                    sqir = queryClient.getRules(address).get(TIMEOUT, TimeUnit.MILLISECONDS);
                } catch (TimeoutException ignore) {}
                if (sqir == null) {
                    LOGGER.warn("Failed to get Query rules, trying again");
                }
            }
            return sqir;
        } catch (Exception e) {
            LOGGER.error("Failed to get Query rules", e);
            reconnect();
            return getQueryRules();
        }
    }

    public SourceQueryPlayerResponse getQueryPlayers() {
        try {
            return queryClient.getPlayers(address).get(TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            LOGGER.error("Failed to get Query players", e);
            reconnect();
            return getQueryPlayers();
        }
    }
}
