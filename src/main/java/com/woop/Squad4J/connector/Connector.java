package com.woop.Squad4J.connector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Robert Engle
 * <p>
 * Represents an abstraction of a connector.
 */
public abstract class Connector {
    private static final Logger LOGGER = LoggerFactory.getLogger(Connector.class);
    private static final List<String> connectorNames = new ArrayList<>();
    private final String connectorName;

    protected Connector(String connectorName) {
        if (connectorNames.contains(connectorName)) {
            LOGGER.error("A connector with name {} already exists.", connectorName);
            throw new IllegalStateException("A connector with that name has already been instantiated.");
        }
        this.connectorName = connectorName;
        connectorNames.add(connectorName);
    }

    public String getConnectorName() {
        return connectorName;
    }
}
