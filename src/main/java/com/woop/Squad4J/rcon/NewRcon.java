package com.woop.Squad4J.rcon;

import com.ibasco.agql.core.util.ConnectOptions;
import com.ibasco.agql.core.util.FailsafeOptions;
import com.ibasco.agql.core.util.GeneralOptions;
import com.ibasco.agql.protocols.valve.source.query.rcon.SourceRconClient;
import com.ibasco.agql.protocols.valve.source.query.rcon.SourceRconOptions;
import com.ibasco.agql.protocols.valve.source.query.rcon.exceptions.RconAuthException;
import com.ibasco.agql.protocols.valve.source.query.rcon.message.SourceRconAuthResponse;
import com.ibasco.agql.protocols.valve.source.query.rcon.message.SourceRconCmdResponse;
import com.woop.Squad4J.util.ConfigLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class NewRcon {
    private static final Logger LOGGER = LoggerFactory.getLogger(NewRcon.class);
    private static final String host = ConfigLoader.get("$.server.host", String.class);
    private static final Integer port = ConfigLoader.get("$.server.rconPort", Integer.class);
    private static final String password = ConfigLoader.get("$.server.rconPassword", String.class);
    final static SourceRconOptions options = SourceRconOptions.builder()
            .option(SourceRconOptions.USE_TERMINATOR_PACKET, false)
            /*.option(ConnectOptions.FAILSAFE_RETRY_ENABLED, false)
            .option(ConnectOptions.FAILSAFE_CIRCBREAKER_ENABLED, false)
            .option(ConnectOptions.FAILSAFE_ENABLED, false)
            .option(ConnectOptions.FAILSAFE_RATELIMIT_ENABLED, false)
            .option(FailsafeOptions.FAILSAFE_CIRCBREAKER_ENABLED, false)
            .option(FailsafeOptions.FAILSAFE_ENABLED, true)
            .option(FailsafeOptions.FAILSAFE_RATELIMIT_ENABLED, false)
            .option(FailsafeOptions.FAILSAFE_RETRY_ENABLED, false)
            .option(GeneralOptions.CONNECTION_POOLING, false)
            .option(SourceRconOptions.REAUTHENTICATE, true)*/
            .build();
    private static SourceRconClient rconClient;
    private static InetSocketAddress address;

    public static void init () {
        connect();
    }

    private static void connect() {
        LOGGER.info("Connecting . . .");
        try {
            rconClient = new SourceRconClient(options);
            address = new InetSocketAddress(host, port);
            SourceRconAuthResponse response = rconClient.authenticate(address, password.getBytes()).join();
            if (!response.isAuthenticated()) {
                LOGGER.error("Failed to authenticate with server (Reason: " + response.getReason()+ ", Code: " + response.getReasonCode().name() + ")");
                return;
            }
        } catch (RconAuthException ex) {
            LOGGER.error("Failed to authenticate with server (Reason: " + ex.getReason() + ")");
            ex.printStackTrace(System.err);
        }
        LOGGER.info("Successfully authenticated/connected with server");
    }

    public static String command(String command) {
        SourceRconCmdResponse res = null;
        while (res == null) {
            try {
                //System.out.println("new command: " + command);
                CompletableFuture<SourceRconCmdResponse> cf = rconClient.execute(address, command);
                //System.out.println("execute complete");
                res = cf.get(3, TimeUnit.SECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                LOGGER.warn("Exception while trying execute rcon command: " + command + ", trying again", e);
                /*System.out.println(rconClient.getStatistics());
                rconClient.printConnectionStats(System.out::println);
                rconClient.printExecutorStats(System.out::println);*/
                reconnect();
                /*System.out.println(rconClient.getStatistics());
                rconClient.printConnectionStats(System.out::println);
                rconClient.printExecutorStats(System.out::println);*/
            }
        }
        //System.out.println("join complete: "/* + res.getResult()*/);
        System.out.println(res.getResult());
        return res.getResult();
    }

    public static void reconnect() {
        LOGGER.warn("Reconnecting . . .");
        close();
        connect();
        LOGGER.warn("Reconnected successfully");
    }

    public static void close() {
        LOGGER.info("Closing connections . . .");
        rconClient.invalidate();
        rconClient.cleanup(true);
        LOGGER.info("Connection closed successfully");
    }
}
