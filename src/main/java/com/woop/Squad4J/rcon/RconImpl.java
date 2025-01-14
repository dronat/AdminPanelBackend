package com.woop.Squad4J.rcon;

import com.woop.Squad4J.rcon.ex.AuthenticationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * Class which interfaces with the RCON server, constructing and sending RCON packets as necessary.
 *
 * <a href=https://github.com/roengle/SimpleRcon>SimpleRcon implementation</a>
 * <p>
 * Source RCON Protocol:
 * <a href="https://developer.valvesoftware.com/wiki/Source_RCON_Protocol">Source RCON Protocol Documentation</a>
 *
 * @author Robert Engle
 */

//TODO: Improve implementation for slower connections
public class RconImpl {
    public static final int SERVERDATA_RESPONSE_VALUE = 0;
    //This one isn't technically defined in Source RCON Protocol, but is used for some games such as Squad
    public static final int SERVERDATA_BROADCAST = 1;
    public static final int SERVERDATA_EXECCOMMAND = 2;
    public static final int SERVERDATA_AUTH_RESPONSE = 2;
    public static final int SERVERDATA_AUTH = 3;
    private static final Logger LOGGER = LoggerFactory.getLogger(RconImpl.class);
    private final String host;
    private final Integer port;
    private final byte[] password;
    private final Object sync = new Object();
    private final Random rand = new Random();
    private final List<Consumer<RconPacket>> onPacketConsumers = new ArrayList<>();
    private Socket socket;
    private int requestId;
    private volatile Queue<RconPacket> commandResponsePackets = new LinkedList<>();

    /**
     * Constructs a {@link RconImpl} object with the given connection properties.
     *
     * @param host     the host address of the RCON server. Can be a FQDN or IP address
     * @param port     the port the RCON server monitors. Default for squad is 21114
     * @param password a {@link String} of the password
     */
    protected RconImpl(String host, Integer port, String password) throws AuthenticationException {
        this(host, port, password.getBytes());
    }

    /**
     * Helper method that's used to construct a {@link RconImpl} object with the given connection properties.
     *
     * @param host     the host address of the RCON server. Can be a FQDN or IP address
     * @param port     the port the RCON server monitors. Default for squad is 21114
     * @param password a byte array representing the password to logon to the RCON server with.
     */
    private RconImpl(String host, Integer port, byte[] password) throws AuthenticationException {
        this.host = host;
        this.port = port;
        this.password = password;
        connect(this.host, this.port, this.password);
        new Thread(() -> {
            try {
                onRconPacket(rconPacket -> {
                    if (rconPacket.getType() == SERVERDATA_RESPONSE_VALUE
                            && !rconPacket.getPayloadAsString().equals("")) {
                        commandResponsePackets.add(rconPacket);
                    }
                });

                boolean multiPacketLikely = false;

                while (true) {
                    while (multiPacketLikely || socketHasData()) {
                        multiPacketLikely = false;
                        RconPacket pak = read(socket.getInputStream());
                        if (pak == null) {
                            break;
                        }
                        if (pak.getSize() > 4098) {
                            multiPacketLikely = true;
                            LOGGER.trace("RCON response most likely has multi-packet response.");
                        }
                        for (Consumer<RconPacket> func : onPacketConsumers) {
                            func.accept(pak);
                        }
                        pak = null;
                    }
                }
            } catch (IOException e) {
                LOGGER.error("I/O error with socket stream.", e);
            }
        }, "rcon").start();
    }

    /* Helper methods */
    private static int getPacketLength(int bodyLength) {
        // 4 bytes for length + x bytes for body length
        return 4 + bodyLength;
    }

    private static int getBodyLength(int payloadLength) {
        // 4 bytes for requestId, 4 bytes for type, x bytes for payload, 2 bytes for two null bytes
        return 4 + 4 + payloadLength + 2;
    }

    /**
     * Function that takes in a {@link Consumer} to consume every time a RCON packet is received. The consumer
     * takes in the supplied {@link RconPacket}
     *
     * @param func the {@link Consumer} to be consumed for each {@link RconPacket} retrieved.
     */
    protected void onRconPacket(Consumer<RconPacket> func) {
        onPacketConsumers.add(func);
    }

    /**
     * Sends a given command to the RCON server and returns the response if one is sent.
     * <p>
     * May wait for response to be sent be finishing execution.
     * <p>
     * If command returns output longer than the maximum RCON packet size, method stiches together outputs
     * that are sent through mulitple packets.
     *
     * @param command the command to output
     * @return the output of the command sent if the RCON server returns one
     */
    protected synchronized String command(String command) {

        final AtomicReference<String> response = new AtomicReference<>();
        response.set("");

        //New request ID
        requestId = rand.nextInt();
        //Make sure request ID isn't 0 or -1
        while (requestId == 0 || requestId == -1) {
            requestId = rand.nextInt();
        }
        //Keep local requestId incase it's changed by another thread
        int thisRequestId = requestId;
        //Asynchronously execute command helper method
        CompletableFuture<Void> future = command(command.getBytes(StandardCharsets.UTF_8));
        try {
            future.get(1, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException e) {
            LOGGER.error(e.getMessage());
        } catch (TimeoutException e) {
            LOGGER.debug("Command {} timed out. This is expected if the command isn't supposed to respond.", command);
        }
        //New list of items to remove from collected list of command responses.
        Collection<RconPacket> removals = new ArrayList<>();

        //Iterate through each command response packet received, and look for those in response to the current command
        commandResponsePackets.forEach(pak -> {
            if (pak.getRequestId() == thisRequestId) {
                //If so, append the packet's payload to the output string
                response.set(response.get().concat(pak.getPayloadAsString()));
                //Mark this packet for removal from the list so it doesn't grow too large
                removals.add(pak);
            }
        });
        //Once done iterating, remove all the items marked for removal
        commandResponsePackets.removeAll(removals);
        return response.get();
    }

    /**
     * Private helper method for {@link RconImpl#command(String)}.
     *
     * @param payload a byte array of the payload for the command
     * @return a {@link CompletableFuture<Void>} representing the state of execution for the command
     */
    private synchronized CompletableFuture<Void> command(byte[] payload) {
        send(SERVERDATA_EXECCOMMAND, payload);
        return CompletableFuture.runAsync(this::waitUntilNoData);
    }

    /**
     * Method to reconnect socket and re-authenticate.
     *
     * @throws IOException
     */
    private void reconnect() {
        LOGGER.warn("Reconnecting to RCON server . . .");
        try {
            Thread.sleep(1000);
            connect(this.host, this.port, this.password);
        } catch (AuthenticationException | InterruptedException ex) {
            LOGGER.error("Error authenticating with RCON server.");
            LOGGER.error(ex.getMessage());
            System.exit(1);
        }
        LOGGER.warn("Rcon reconnected");
    }

    /**
     * Disconnects the working socket.
     *
     * @throws IOException
     */
    private void disconnect() throws IOException {
        socket.close();
    }

    /**
     * Tests if the socket has data to retrieve from its input stream.
     *
     * @return true if there is data to be retrieved, false if not
     */
    private boolean socketHasData() {
        boolean status = false;
        try {
            Thread.sleep(100);
            if (socket.isClosed()) {
                LOGGER.warn("Socket is closed by unknown reason");
                reconnect();
            }
            status = socket.getInputStream().available() > 0;
        } catch (IOException e) {
            LOGGER.error("Error with socket stream", e);
            reconnect();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return status;
    }

    /**
     * Method that waits until there is no more data to receive from the socket.
     * <p>
     * Should only be used asynchronously as this will hold up execution of a synchronously-running program.
     */
    private void waitUntilNoData() {
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            LOGGER.error("Thread error", e);
        }
        while (socketHasData()) {
        }
    }

    /**
     * Connects a socket to an RCON server, then attempts to authenticate with the given password.
     *
     * @param host     the host to connect to
     * @param port     the port the RCON server is listening on
     * @param password a byte array of the password to authenticate with
     * @throws AuthenticationException when the password is incorrect
     */
    private void connect(String host, Integer port, byte[] password) throws AuthenticationException {
        LOGGER.info("Connecting to rcon . . . ");
        synchronized (sync) {
            try {
                //New request id
                requestId = rand.nextInt();
                while (requestId == -1 || requestId == 0) {
                    requestId = rand.nextInt();
                }

                //Can't reuse socket, so make a new one
                socket = new Socket(host, port);
                socket.setSoTimeout(5000);
            } catch (IOException e) {
                LOGGER.error("Error creating socket");
                LOGGER.error(e.getMessage());
            }
        }
        send(SERVERDATA_AUTH, password);
        try {
            //Read first empty SERVERDATA_RESPONSE_VALUE packet
            read(socket.getInputStream());
            //Read SERVERDATA_AUTH_RESPONSE with authentication status
            RconPacket pak2 = read(socket.getInputStream());
            //If auth response packet has request id of -1, then it is an incorrect password
            if (pak2 != null && pak2.getRequestId() == -1) {
                LOGGER.error("Incorrect RCON password.");
                throw new AuthenticationException("Incorrect password.");
            }
        } catch (IOException e) {
            LOGGER.error("Error with socket stream", e);
        }
        LOGGER.info("Rcon successfully connected");
    }

    /**
     * Sends data to the RCON server, given a packet type and payload
     *
     * @param type    the type of packet as defined by the Source RCON Protocol
     * @param payload a byte array of the payload to send to the RCON server
     */
    private void send(int type, byte[] payload) {
        synchronized (sync) {
            try {
                write(socket.getOutputStream(), requestId, type, payload);
            } catch (SocketException se) {
                // Close the socket if something happens
                try {
                    socket.close();
                } catch (IOException e) {
                    LOGGER.error("Error closing socket");
                    LOGGER.error(e.getMessage());
                }
            } catch (IOException e) {
                LOGGER.error("Error writing to socket output stream");
                LOGGER.error(e.getMessage());
            }
        }
    }

    /**
     * Writes data to the socket for this Rcon's {@link OutputStream} and flushes it.
     *
     * @param out       the {@link OutputStream} to write to
     * @param requestId the id of the request to sent
     * @param type      the type of packet
     * @param payload   the payload being sent
     * @throws IOException
     */
    private void write(OutputStream out, int requestId, int type, byte[] payload) throws IOException {
        int bodyLength = getBodyLength(payload.length);
        int packetLength = getPacketLength(bodyLength);

        ByteBuffer buffer = ByteBuffer.allocate(packetLength);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        //Packet fields
        buffer.putInt(bodyLength);
        buffer.putInt(requestId);
        buffer.putInt(type);
        buffer.put(payload);

        //Null byte terminator for body
        buffer.put((byte) 0);
        //Null byte terminator for packet
        buffer.put((byte) 0);

        // Bye bye!
        out.write(buffer.array());
        out.flush();
    }

    /**
     * Reads data from this socket's {@link InputStream} and returns individual {@link RconPacket}s
     *
     * @param in the socket's {@link InputStream} to read from
     * @return the {@link RconPacket} read
     * @throws IOException
     */
    private RconPacket read(InputStream in) throws IOException {
        // Header is 3 4-bytes ints
        byte[] header = new byte[4 * 3];

        try {
            synchronized (sync) {
                // Read the 3 ints
                if (in.read(header) == -1) {
                    return null;
                }
                // Use a bytebuffer in little endian to read the first 3 ints
                ByteBuffer buffer = ByteBuffer.wrap(header);
                buffer.order(ByteOrder.LITTLE_ENDIAN);

                int length = buffer.getInt();
                if (length < 10) {
                    LOGGER.warn("Rcon pocket have length less then 10");
                    return null;
                }
                int requestId = buffer.getInt();
                int type = buffer.getInt();

                byte[] payload;
                try {
                    payload = new byte[length - 10];
                } catch (NegativeArraySizeException e) {
                    throw new RuntimeException(e);
                }

                DataInputStream dis = new DataInputStream(in);

                // Read the full payload
                try {
                    dis.readFully(payload);
                } catch (Exception e) {
                    LOGGER.warn("Exception while trying read DataInputStream in Payload", e);
                    return null;
                }


                // Read the null bytes
                dis.read(new byte[2]);
                /*try {
                    System.out.println("Received packet: \n" + new String(payload));
                } catch (Exception e) {
                    LOGGER.error("Cant convert byte to string");
                }*/
                return new RconPacket(new Date(), length, requestId, type, payload);
            }
        } catch (Exception e) {
            LOGGER.error("Error reading packet", e);
            reconnect();
        }
        return null;
    }

}