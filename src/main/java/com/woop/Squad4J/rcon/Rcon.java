package com.woop.Squad4J.rcon;

import com.woop.Squad4J.rcon.ex.AuthenticationException;
import com.woop.Squad4J.server.LogParser;
import com.woop.Squad4J.util.ConfigLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Robert Engle
 * <p>
 * Singleton wrapper for RconImpl
 */
public class Rcon {
    private static final Logger LOGGER = LoggerFactory.getLogger(Rcon.class);

    private static RconImpl rconImpl;
    private static boolean initialized = false;

    private Rcon() {
    }

    public static void init() {
        if (initialized)
            throw new IllegalStateException("Rcon has already been initialized, you cannot re-initialize it.");

        String host = ConfigLoader.get("$.server.host", String.class);
        Integer port = ConfigLoader.get("$.server.rconPort", Integer.class);
        String password = ConfigLoader.get("$.server.rconPassword", String.class);
        try {
            LOGGER.info("Connecting to RCON server.");
            rconImpl = new RconImpl(host, port, password);
        } catch (AuthenticationException e) {
            LOGGER.error("Error authenticating with RCON server.");
            LOGGER.error(e.getMessage());
            System.exit(1);
        }
        LOGGER.info("Connected to RCON server.");

        rconImpl.onRconPacket(rconPacket -> {
            if (rconPacket.getType() == RconImpl.SERVERDATA_BROADCAST) {
                LOGGER.info("\u001B[46m \u001B[30m" + rconPacket.getPayloadAsString() + "\u001B[0m");
                LogParser.parseLine(rconPacket.getPayloadAsString());
            }
        });

        initialized = true;

    }

    public static String command(String cmd) {
        return initialized ? rconImpl.command(cmd) : "";
    }

}
