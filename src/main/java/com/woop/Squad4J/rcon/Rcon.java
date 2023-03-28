package com.woop.Squad4J.rcon;

import com.woop.Squad4J.event.rcon.EnteredInAdminCameraEvent;
import com.woop.Squad4J.event.rcon.LeftFromAdminCameraEvent;
import com.woop.Squad4J.rcon.ex.AuthenticationException;
import com.woop.Squad4J.server.EventEmitter;
import com.woop.Squad4J.server.LogParser;
import com.woop.Squad4J.util.ConfigLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.woop.Squad4J.event.EventType.ENTERED_IN_ADMIN_CAM;
import static com.woop.Squad4J.event.EventType.LEFT_FROM_ADMIN_CAM;

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
                if (rconPacket.getPayloadAsString().matches("\\[SteamID:([0-9]{17})] (.+?) has possessed admin camera.")) {
                    Matcher matcher = Pattern.compile("\\[SteamID:([0-9]{17})] (.+?) has possessed admin camera.").matcher(rconPacket.getPayloadAsString());
                    if (matcher.find()) {
                        EventEmitter.emit(
                                new EnteredInAdminCameraEvent(
                                        new Date(),
                                        ENTERED_IN_ADMIN_CAM,
                                        Long.parseLong(matcher.group(1).strip()),
                                        matcher.group(2)
                                )
                        );
                    }
                } else if (rconPacket.getPayloadAsString().matches("\\[SteamID:([0-9]{17})] (.+?) has unpossessed admin camera.")) {
                    Matcher matcher = Pattern.compile("\\[SteamID:([0-9]{17})] (.+?) has unpossessed admin camera.").matcher(rconPacket.getPayloadAsString());
                    if (matcher.find()) {
                        EventEmitter.emit(
                                new LeftFromAdminCameraEvent(
                                        new Date(),
                                        LEFT_FROM_ADMIN_CAM,
                                        Long.parseLong(matcher.group(1).strip()),
                                        matcher.group(2)
                                )
                        );
                    }
                }
            }
        });

        initialized = true;

    }

    public static String command(String cmd) {
        return initialized ? rconImpl.command(cmd) : "";
    }

}
