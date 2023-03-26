package com.woop.Squad4J.server;

import com.example.adminpanelbackend.db.EntityManager;
import com.example.adminpanelbackend.db.entity.MapEntity;
import com.example.adminpanelbackend.db.entity.PlayerEntity;
import com.example.adminpanelbackend.db.entity.ServerEntity;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.ibasco.agql.protocols.valve.source.query.info.SourceServer;
import com.jayway.jsonpath.JsonPathException;
import com.woop.Squad4J.dto.rcon.*;
import com.woop.Squad4J.event.Event;
import com.woop.Squad4J.event.EventType;
import com.woop.Squad4J.event.a2s.A2SUpdatedEvent;
import com.woop.Squad4J.event.logparser.*;
import com.woop.Squad4J.event.rcon.*;
import com.woop.Squad4J.rcon.Rcon;
import com.woop.Squad4J.util.ConfigLoader;
import lombok.Getter;
import org.apache.commons.lang3.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

import static com.example.adminpanelbackend.ActionEnum.*;

/**
 * Class to represent the Squad server and it's attributes in memory.
 * <p>
 * Provides getters for static variables in memory, which are updated through asynchronous threads and by
 * receiving events.
 *
 * @author Robert Engle
 */
public class SquadServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(SquadServer.class);
    private static final EntityManager entityManager = new EntityManager();
    private static final int SERVER_ID = ConfigLoader.get("server.id", Integer.class);
    private static final BiMap<String, Long> nameSteamIds = HashBiMap.create();
    private static boolean initialized = false;
    private static List<Long> playersOnControl;
    private static List<Long> admins;
    private static List<Long> adminsInGame = new ArrayList<>();
    private static Collection<OnlinePlayer> adminsInAdminCam;
    private static Collection<OnlinePlayer> onlinePlayers;
    private static Collection<DisconnectedPlayer> disconnectedPlayers;
    private static Collection<Squad> squads;
    private static Collection<Team> teams;
    private static Collection<ChatMessageEvent> chatMessages = new LinkedList<>();

    private static MapEntity currentMapEntity;
    @Getter
    private static String currentLayer;
    @Getter
    private static String lastKnownLayer;
    @Getter
    private static String teamOneName;
    @Getter
    private static String teamTwoName;
    @Getter
    private static String currentMap;
    @Getter
    private static String nextLayer;
    @Getter
    private static String nextMap;

    @Getter
    private static String serverName;
    @Getter
    private static Integer maxPlayers;
    @Getter
    private static Integer publicSlots;
    @Getter
    private static Integer reserveSlots;

    @Getter
    private static Integer playerCount;
    @Getter
    private static Integer publicQueue;
    @Getter
    private static Integer reserveQueue;

    @Getter
    private static String gameVersion;
    @Getter
    private static Double matchTimeout;

    @Getter
    private static Integer playTime;

    @Getter
    private static Integer maxTickRate;
    @Getter
    private static Double serverTickRate;

    @Getter
    private static String mostRecentWinner;

    private SquadServer() {

    }

    public static void init() {
        if (initialized)
            throw new IllegalStateException("This class is already initialized.");
        entityManager.init();

        String sourceRef = null;
        //TODO: Improve admin reading to take union all permissions for UNIQUE ADMINS across all files. Currently, this logic actually sucks
        try {
            /*Integer numAdminLists = ConfigLoader.get("server.adminLists.length()", Integer.class);
            for (int i = 0; i < numAdminLists; i++) {
                Map<Object, Object> adminListMap = ConfigLoader.get("server.adminLists[" + i + "]", Map.class);
                boolean isRemote = adminListMap.get("type").equals("remote");
                String source = (String) adminListMap.get("source");
                sourceRef = source;
                LOGGER.trace("Reading admins from {}", source);
                //Only use add all for initialization step, when updating the list will need to be replaced
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    try {
                        adminSteamIds.addAll(
                                isRemote ? AdminListReader.getAdminIdsFromRemote(source) : AdminListReader.getAdminIdsFromLocal(source)
                        );

                        adminSteamIds = adminSteamIds.stream().distinct().collect(Collectors.toList());
                    } catch (FileNotFoundException fe) {
                        LOGGER.error("File {} not found", source, fe);
                    } catch (IOException e) {
                        LOGGER.error("IO Error", e);
                    }
                });
                try {
                    future.get(5, TimeUnit.SECONDS);
                } catch (ExecutionException e) {
                    LOGGER.error("Asynchronous thread threw an exception", e);
                } catch (InterruptedException e) {
                    LOGGER.error("Asynchronous thread interrupted", e);
                    //TODO: Test this for compliance
                    Thread.currentThread().interrupt();
                    future.cancel(true);
                } catch (TimeoutException e) {
                    LOGGER.warn("Timeout reading admin list from {}", sourceRef);
                }

                LOGGER.trace("Read {} admins from {}", adminSteamIds.size(), source);
            }*/

            //Update A2S and RCON information first so Squad server has attributes for them in memory
            ServerEntity serverEntity = entityManager.getServerById(SERVER_ID);
            if (serverEntity == null) {
                entityManager.update(
                        new ServerEntity()
                                .setId(SERVER_ID)
                                .setShortName(ConfigLoader.get("server.name", String.class))
                );
            } else {
                entityManager.update(serverEntity.setShortName(ConfigLoader.get("server.name", String.class)));
            }
            Runnable runnable = () -> {
                ServerEntity tmpServerEntity = entityManager.getServerById(SERVER_ID);
                try {
                    Thread.sleep(60000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                entityManager.update(Objects.requireNonNull(tmpServerEntity).setFullName(serverName));
            };
            new Thread(runnable).start();

            playersOnControl = entityManager.getPlayersOnControl();
            admins = entityManager.getActiveAdminsSteamId();
            RconUpdater.updateRcon();
            A2SUpdater.updateA2S();

            if (onlinePlayers == null) {
                LOGGER.error("Cant init onlinePlayers");
                System.exit(1);
            }
            getOnlinePlayers().forEach(onlinePlayer -> {
                if (!entityManager.isPlayerExist(onlinePlayer.getSteamId())) {
                    entityManager.addPlayer(onlinePlayer.getSteamId(), onlinePlayer.getName());
                } else {
                    PlayerEntity playerEntity = entityManager.getPlayerBySteamId(onlinePlayer.getSteamId());
                    if (!onlinePlayer.getName().equals(playerEntity.getName())) {
                        //LOGGER.info("\u001B[46m \u001B[30m (onInit) Player change name from '{}' to '{}' \u001B[0m", playerEntity.getName(), onlinePlayer.getName());
                        //String oldName = playerEntity.getName();
                        playerEntity.setName(onlinePlayer.getName());
                        entityManager.update(playerEntity);
                        //entityManager.addPlayerNote(playerEntity, "Игрок изменил имя с '" + oldName + "' на '" + onlinePlayer.getName() + "'");
                    }
                }
            });

            //Initialize service to update A2S and RCON information every 30 seconds.
            A2SUpdater.init();
            RconUpdater.init();
            currentMapEntity = entityManager.getMapByOneOfNames(currentLayer);
            lastKnownLayer = currentLayer;


        } catch (JsonPathException jsexp) {
            LOGGER.error("Error reading admin list configuration.", jsexp);
        }

        //LOGGER.trace("Parsed {} unique admins", adminSteamIds.size());

        initialized = true;
    }

    /**
     * Receives all events from {@link EventEmitter}, updating the server as necessary.
     *
     * @param ev the {@link Event} emitted by {@link EventEmitter}
     */
    protected static void receiveEvent(final Event ev) {
        EventType type = ev.getType();

        //TODO: Add receiving of tick rate event to update current tick rate field
        switch (type) {
            //A2S
            case A2S_UPDATED:
                LOGGER.trace("Updating SquadServer A2S info");
                A2SUpdatedEvent a2sEvent = (A2SUpdatedEvent) ev;
               /* A2SInfoResponse info = a2sEvent.getResponse().getInfo();
                A2SRulesResponse rules = a2sEvent.getResponse().getRules();*/
                SourceServer info = a2sEvent.getResponse().getInfo().getResult();
                Map<String, String> rules = a2sEvent.getResponse().getRules().getResult();

                if (info == null || rules == null)
                    break;

                serverName = info.getName();
                maxPlayers = info.getMaxPlayers();

                publicSlots = Integer.valueOf(rules.get("NUMPUBCONN"));
                reserveSlots = Integer.valueOf(rules.get("NUMPRIVCONN"));

                playerCount = Integer.valueOf(rules.get("PlayerCount_i"));
                publicQueue = Integer.valueOf(rules.get("PublicQueue_i"));
                reserveQueue = Integer.valueOf(rules.get("ReservedQueue_i"));

                matchTimeout = Double.valueOf(rules.get("MatchTimeout_f"));
                playTime = Integer.valueOf(rules.get("PLAYTIME_i"));
                gameVersion = info.getGameVersion();

                if (currentLayer == null || currentMap == null) {
                    LOGGER.error("Can't get current layer from Rcon on init");
                    System.exit(1);
                }

                teamOneName = rules.get("TeamOne_s").replace(currentLayer.replace(" ", "_"), "").replace("_", "").trim();
                teamTwoName = rules.get("TeamTwo_s").replace(currentLayer.replace(" ", "_"), "").replace("_", "").trim();

                LOGGER.trace("Done updating SquadServer A2S info");

                break;
            //Log Parser
            case NEW_GAME:
                LOGGER.trace("Updating SquadServer for NEW_GAME");
                NewGameEvent newGameEvent = (NewGameEvent) ev;
                entityManager.addLayer(newGameEvent.getLayerName());
                maxTickRate = newGameEvent.getMaxTickRate();
                A2SUpdater.updateA2S();
                RconUpdater.updateRcon();
                if (currentLayer.toLowerCase().contains("raas")) {
                    Rcon.command("AdminSetFogOfWar 0");
                }
                chatMessages.clear();
                //TODO: Update admins since these can change between games
                LOGGER.trace("Done updating SquadServer for NEW_GAME");
                break;
            case SERVER_TICK_RATE:
                LOGGER.trace("Updating SquadServer for SERVER_TICK_RATE");
                ServerTickRateEvent serverTickRateEvent = (ServerTickRateEvent) ev;
                serverTickRate = serverTickRateEvent.getTickRate();
                LOGGER.trace("Done updating SquadServer for SERVER_TICK_RATE");
                break;
            case PLAYER_CONNECTED:
                //TODO: Update admins if player connected has steam id in adminSteamIds
                break;
            case PLAYER_DISCONNECTED:
                LOGGER.trace("Updating SquadServer for PLAYER_DISCONNECTED");
                PlayerDisconnectedEvent playerDisconnectedEvent = (PlayerDisconnectedEvent) ev;
                nameSteamIds.inverse().remove(playerDisconnectedEvent.getSteamId());
                if (admins.contains(playerDisconnectedEvent.getSteamId())) {
                    adminsInGame.remove(playerDisconnectedEvent.getSteamId());
                }
                //TODO: Update admins if player disconnected has steam id in adminSteamIds
                LOGGER.trace("Done updating SquadServer for PLAYER_DISCONNECTED");
                break;
            case ROUND_WINNER:
                LOGGER.trace("Updating SquadServer for ROUND_WINNER");
                RoundWinnerEvent roundWinnerEvent = (RoundWinnerEvent) ev;
                mostRecentWinner = roundWinnerEvent.getWinningFaction();
                LOGGER.trace("Done updating SquadServer for ROUDN_WINNER");
                break;
            case STEAMID_CONNECTED:
                SteamIdConnectedEvent steamidConnectedEvent = (SteamIdConnectedEvent) ev;
                if (admins.contains(steamidConnectedEvent.getSteamId())) {
                    adminsInGame.add(steamidConnectedEvent.getSteamId());
                }
                Runnable sceRunnable = () -> {
                    int i = 0;
                    while (i < 20) {
                        try {
                            LOGGER.trace("Updating SquadServer for STEAMID_CONNECTED");
                            nameSteamIds.put(steamidConnectedEvent.getName(), steamidConnectedEvent.getSteamId());
                            if (!entityManager.isPlayerExist(steamidConnectedEvent.getSteamId())) {
                                /*OnlinePlayer onlinePlayer = onlinePlayers.stream()
                                        .filter(onlPlayer -> onlPlayer.getSteamId() == steamidConnectedEvent.getSteamId())
                                        .findFirst()
                                        .orElseThrow();*/
                                entityManager.addPlayer(steamidConnectedEvent.getSteamId(), steamidConnectedEvent.getName());
                            } else {
                                PlayerEntity playerEntity = entityManager.getPlayerBySteamId(steamidConnectedEvent.getSteamId());
                                /*OnlinePlayer onlinePlayer = onlinePlayers.stream()
                                        .filter(onlPlayer -> onlPlayer.getSteamId() == steamidConnectedEvent.getSteamId())
                                        .findFirst()
                                        .orElseThrow();*/
                                if (!playerEntity.getName().equals(steamidConnectedEvent.getName())) {
                                    //LOGGER.info("\u001B[46m \u001B[30m (onSteamIdConnected) Player change name from '{}' to '{}' \u001B[0m", playerEntity.getName(), onlinePlayer.getName());
                                    //String oldName = playerEntity.getName();
                                    playerEntity.setName(steamidConnectedEvent.getName());
                                    entityManager.update(playerEntity);
                                    //entityManager.addPlayerNote(playerEntity, "Игрок изменил имя с '" + oldName + "' на '" + playerEntity.getName() + "'");
                                }
                            }
                            LOGGER.trace("Done updating SquadServer for STEAMID_CONNECTED");
                            return;
                        } catch (NoSuchElementException e) {
                            //LOGGER.warn("Player {} not initialized in RCON", steamidConnectedEvent.getSteamId());
                            try {
                                Thread.sleep(3000);
                            } catch (InterruptedException ex) {
                                throw new RuntimeException(ex);
                            }
                            i++;
                        }
                    }
                    LOGGER.warn("Cant get info from rcon after 10 retrying by player " + steamidConnectedEvent.getSteamId());
                };
                new Thread(sceRunnable).start();
                break;
            //Rcon
            case CHAT_MESSAGE:
                ChatMessageEvent cme = (ChatMessageEvent) ev;
                chatMessages.add(cme);
                if (cme.getMessage().toLowerCase().contains("!report")) {
                    String msg = " Репорт от " + cme.getPlayerName() + ": " + cme.getMessage().toLowerCase().replace("!report", "").trim();
                    new Thread(() ->
                            adminsInGame.forEach(adminSteamId ->
                                    Rcon.command("AdminWarn " + adminSteamId + msg)
                            )
                    ).start();
                }
                entityManager.addPlayerMessage(cme.getSteamId(), cme.getChatType(), cme.getMessage());
                LOGGER.trace("'{}' send new message '{}' in chat '{}'", cme.getPlayerName(), cme.getMessage(), cme.getChatType());
                break;
            case LAYERINFO_UPDATED:
                LOGGER.trace("Updating SquadServer for LAYERINFO_UPDATED");
                currentMap = ((LayerInfoUpdatedEvent) ev).getCurrentMap();
                currentLayer = ((LayerInfoUpdatedEvent) ev).getCurrentLayer();
                if (!currentLayer.equals(lastKnownLayer)) {
                    currentMapEntity = entityManager.getMapByOneOfNames(currentLayer);
                    entityManager.update(currentMapEntity.setNumOfGames(currentMapEntity.getNumOfGames() + 1));
                    lastKnownLayer = currentLayer;
                    if (RotationListener.isRotationHaveMaps()) {
                        String map = RotationListener.incrementNextMapAndGet();
                        LOGGER.info("Setting next map by rotation: " + map);
                        String response = Rcon.command("AdminSetNextLayer " + map);
                        if (response == null || response.isEmpty()) {
                            LOGGER.error("Error while trying set next map to '" + map + "', because RCON returned null or empty string in response");
                            LOGGER.error("RCON RESPONSE: " + response);
                        }
                        entityManager.addAdminActionInLog(1, null, CHANGE_NEXT_LAYER, map);
                        LOGGER.info("Next map '" + map + "' was set");
                    }
                }
                nextMap = ((LayerInfoUpdatedEvent) ev).getNextMap();
                nextLayer = ((LayerInfoUpdatedEvent) ev).getNextLayer();
                LOGGER.trace("Done updating SquadServer for LAYERINFO_UPDATED");
                break;
            case PLAYER_BANNED:
                LOGGER.trace("Updating SquadServer for PLAYER_BANNED");
                PlayerBannedEvent playerBannedEvent = (PlayerBannedEvent) ev;
                nameSteamIds.inverse().remove(playerBannedEvent.getSteamId());
                LOGGER.trace("Done updating SquadServer for PLAYER_BANNED");
                break;
            case PLAYER_KICKED:
                LOGGER.trace("Updating SquadServer for PLAYER_KICKED");
                PlayerKickedEvent playerKickedEvent = (PlayerKickedEvent) ev;
                nameSteamIds.inverse().remove(playerKickedEvent.getSteamId());
                LOGGER.trace("Done updating SquadServer for PLAYER_KICKED");
                break;
            case PLAYERLIST_UPDATED:
                LOGGER.trace("Updating SquadServer for PLAYERLIST_UPDATED");
                PlayerListUpdatedEvent playerListUpdatedEvent = (PlayerListUpdatedEvent) ev;
                onlinePlayers = playerListUpdatedEvent
                        .getOnlinePlayersList()
                        .stream()
                        .peek(player -> {
                            player.setIsOnControl(playersOnControl.contains(player.getSteamId()));
                            player.setIsAdmin(admins.contains(player.getSteamId()));
                        })
                        .collect(Collectors.toList());
                disconnectedPlayers = playerListUpdatedEvent.getDisconnectedPlayersList();
                LOGGER.trace("Done updating SquadServer for PLAYERLIST_UPDATED");
                break;
            case ENTERED_IN_ADMIN_CAM:
                LOGGER.trace("Updating SquadServer for POSSESSED_ADMIN_CAM");
                EnteredInAdminCameraEvent enteredInAdminCameraEvent = (EnteredInAdminCameraEvent) ev;
                getPlayerBySteamId(enteredInAdminCameraEvent.getSteamId()).ifPresent(p -> adminsInAdminCam.add(p));
                entityManager.addAdminActionInLog(enteredInAdminCameraEvent.getSteamId(), null, ENTERED_ADMIN_CAM, null);
                LOGGER.trace("Done updating SquadServer for POSSESSED_ADMIN_CAM");
                break;
            case LEFT_FROM_ADMIN_CAM:
                LOGGER.trace("Updating SquadServer for UNPOSSESSED_ADMIN_CAM");
                LeftFromAdminCameraEvent leftFromAdminCameraEvent = (LeftFromAdminCameraEvent) ev;
                getPlayerBySteamId(leftFromAdminCameraEvent.getSteamId()).ifPresent(p -> adminsInAdminCam.remove(p));
                entityManager.addAdminActionInLog(leftFromAdminCameraEvent.getSteamId(), null, LEFT_FROM_ADMIN_CAM, null);
                LOGGER.trace("Done updating SquadServer for UNPOSSESSED_ADMIN_CAM");
                break;
            case SQUADLIST_UPDATED:
                LOGGER.trace("Updating SquadServer for SQUADLIST_UPDATED");
                SquadAndTeamListsUpdatedEvent squadAndTeamListsUpdatedEvent = (SquadAndTeamListsUpdatedEvent) ev;
                squads = squadAndTeamListsUpdatedEvent.getSquadList();
                teams = squadAndTeamListsUpdatedEvent
                        .getTeamsList()
                        .stream()
                        .peek(elm -> {
                            if (elm.getId() == 1) {
                                elm.setTeamNameShort(currentMapEntity.getTeamOne().getFaction());
                            } else if (elm.getId() == 2) {
                                elm.setTeamNameShort(currentMapEntity.getTeamTwo().getFaction());
                            }
                        }).collect(Collectors.toList());
                LOGGER.trace("Done updating SquadServer for SQUADLIST_UPDATED");
                break;
            default:
                LOGGER.trace("SquadServer received non-supported event. Ignoring.");
        }
    }

    public static OnlineInfo getOnlineTeamsWithSquadsAndPlayers() {
        OnlineInfo onlineInfo = new OnlineInfo();
        getTeams().forEach(team -> onlineInfo.addTeam(SerializationUtils.clone(team)));
        getSquads().forEach(squad -> onlineInfo.getTeamById(squad.getTeamId()).addSquad(SerializationUtils.clone(squad)));
        //getDisconnectedPlayers().forEach(disconnectedPlayer -> onlineInfo.addDisconnectedPlayer(SerializationUtils.clone(disconnectedPlayer)));

        getOnlinePlayers().forEach(onlinePlayer -> {
            if (onlinePlayer.getTeamId() != null && !onlineInfo.getTeams().isEmpty()) {
                if (onlinePlayer.getSquadID() == null) {
                    onlineInfo
                            .getTeamById(onlinePlayer.getTeamId())
                            .addPlayerWithoutSquad(SerializationUtils.clone(onlinePlayer));
                } else {
                    if (onlineInfo.getTeamById(onlinePlayer.getTeamId()).getSquadById(onlinePlayer.getSquadID()) == null) {
                        onlineInfo
                                .getTeamById(onlinePlayer.getTeamId())
                                .addSquad(
                                        new Squad(
                                                onlinePlayer.getTeamId(),
                                                onlinePlayer.getSquadID(),
                                                null,
                                                null,
                                                null,
                                                null,
                                                null)
                                );
                    }
                    onlineInfo
                            .getTeamById(onlinePlayer.getTeamId())
                            .getSquadById(onlinePlayer.getSquadID())
                            .addPlayer(SerializationUtils.clone(onlinePlayer));
                }
            }
        });
        /*if (!onlineInfo.getTeams().isEmpty()) {
            //onlineInfo.getTeamById(1).setTeamNameShort(teamOneName.substring(teamOneName.lastIndexOf("_") + 1));
            //onlineInfo.getTeamById(2).setTeamNameShort(teamTwoName.substring(teamOneName.lastIndexOf("_") + 1));
            try {
                onlineInfo.getTeamById(1).setTeamNameShort(
                        onlineInfo.getTeamById(1)
                                .getSquads()
                                .get(0)
                                .getPlayers()
                                .get(0)
                                .getRole()
                                .substring(0, onlineInfo.getTeamById(1).getSquads().get(0).getPlayers().get(0).getRole().indexOf("_"))
                );
            } catch (Exception ignored) {
            }
            try {
                onlineInfo.getTeamById(2).setTeamNameShort(
                        onlineInfo.getTeamById(2)
                                .getSquads()
                                .get(0)
                                .getPlayers()
                                .get(0)
                                .getRole()
                                .substring(0, onlineInfo.getTeamById(2).getSquads().get(0).getPlayers().get(0).getRole().indexOf("_"))
                );
            } catch (Exception ignored) {
            }
        }*/
        return onlineInfo;
    }

    public static HashMap<String, Object> getServerInfo() {
        return new HashMap<>() {{
            put("teamOne", teamOneName);
            put("teamTwo", teamTwoName);
            put("currentLayer", currentLayer);
            put("currentMap", currentMap);
            put("nextLayer", nextLayer);
            put("nextMap", nextMap);
            put("serverName", serverName);
            put("maxPlayers", maxPlayers);
            put("publicSlots", publicSlots);
            put("reserveSlots", reserveSlots);
            put("playerCount", playerCount);
            put("publicQueue", publicQueue);
            put("reserveQueue", reserveQueue);
            put("gameVersion", gameVersion);
            put("matchTimeout", matchTimeout);
            put("playTime", playTime);
            put("serverTickRate", serverTickRate);
            put("maxTickRate", maxTickRate);
            put("mostRecentWinner", mostRecentWinner);
        }};
    }

    public static Optional<OnlinePlayer> getPlayerBySteamId(long steam64id) {
        return onlinePlayers.stream().filter(player -> player.getSteamId().equals(steam64id)).findFirst();
    }

    public static Collection<OnlinePlayer> getOnlinePlayers() {
        return Collections.unmodifiableCollection(onlinePlayers);
    }

    public static Collection<DisconnectedPlayer> getDisconnectedPlayers() {
        return Collections.unmodifiableCollection(disconnectedPlayers);
    }

    public static Collection<Squad> getSquads() {
        return Collections.unmodifiableCollection(squads);
    }

    public static Collection<Team> getTeams() {
        return Collections.unmodifiableCollection(teams);
    }

    public static Collection<OnlinePlayer> getAdminsInAdminCam() {
        return Collections.unmodifiableCollection(adminsInAdminCam);
    }

    public static Collection<ChatMessageEvent> getChatMessages() {
        return Collections.unmodifiableCollection(chatMessages);
    }

    public static void addPlayerOnControl(long steamId) {
        playersOnControl.add(steamId);
        onlinePlayers.forEach(player -> {
            if (player.getSteamId() == steamId) {
                player.setIsOnControl(true);
            }
        });
    }

    public static void removePlayerFromControl(long steamId) {
        playersOnControl.remove(steamId);
        onlinePlayers.forEach(player -> {
            if (player.getSteamId() == steamId) {
                player.setIsOnControl(false);
            }
        });
    }

    public static void addAdmin(long steamId) {
        admins.add(steamId);
        onlinePlayers.forEach(player -> {
            if (player.getSteamId() == steamId) {
                player.setIsAdmin(true);
            }
        });
    }

    public static void removeAdmin(long steamId) {
        admins.remove(steamId);
        onlinePlayers.forEach(player -> {
            if (player.getSteamId() == steamId) {
                player.setIsAdmin(false);
            }
        });
    }

    public static Collection<Long> getAdmins() {
        return Collections.unmodifiableCollection(admins);
    }

    public static Collection<Long> getPlayersOnControl() {
        return Collections.unmodifiableCollection(playersOnControl);
    }
}
