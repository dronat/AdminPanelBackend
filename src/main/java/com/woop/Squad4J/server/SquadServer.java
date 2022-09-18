package com.woop.Squad4J.server;

import com.example.adminpanelbackend.dataBase.EntityManager;
import com.example.adminpanelbackend.dataBase.entity.PlayerEntity;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.jayway.jsonpath.JsonPathException;
import com.woop.Squad4J.a2s.response.A2SInfoResponse;
import com.woop.Squad4J.a2s.response.A2SRulesResponse;
import com.woop.Squad4J.event.Event;
import com.woop.Squad4J.event.EventType;
import com.woop.Squad4J.event.a2s.A2SUpdatedEvent;
import com.woop.Squad4J.event.logparser.*;
import com.woop.Squad4J.event.rcon.*;
import com.woop.Squad4J.model.*;
import com.woop.Squad4J.rcon.Rcon;
import com.woop.Squad4J.util.AdminListReader;
import com.woop.Squad4J.util.ConfigLoader;
import lombok.Getter;
import org.apache.commons.lang3.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

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

    private static boolean initialized = false;

    private static final EntityManager entityManager = new EntityManager();

    @Getter
    private static Integer id;

    private static final BiMap<String, Long> nameSteamIds = HashBiMap.create();

    private static Collection<OnlinePlayer> onlinePlayers;
    private static Collection<DisconnectedPlayer> disconnectedPlayers;
    private static Collection<Squad> squads;
    private static Collection<Team> teams;

    private static List<String> adminSteamIds = new ArrayList<>();

    private static Collection<OnlinePlayer> admins;
    private static Collection<OnlinePlayer> adminsInAdminCam;

    @Getter
    private static String currentLayer;
    @Getter
    private static String currentMap;
    @Getter
    private static String nextMap;
    @Getter
    private static String nextLayer;

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
    private static Integer maxTickRate;

    @Getter
    private static String mostRecentWinner;

    private SquadServer() {

    }

    public static void init() {
        if (initialized)
            throw new IllegalStateException("This class is already initialized.");

        id = ConfigLoader.get("server.id", Integer.class);
        String sourceRef = null;
        //TODO: Improve admin reading to take union all permissions for UNIQUE ADMINS across all files. Currently, this logic actually sucks
        try {
            Integer numAdminLists = ConfigLoader.get("server.adminLists.length()", Integer.class);
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
            }

            //Update A2S and RCON information first so Squad server has attributes for them in memory
            A2SUpdater.updateA2S();
            RconUpdater.updateRcon();

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

        } catch (JsonPathException jsexp) {
            LOGGER.error("Error reading admin list configuration.", jsexp);
        }

        LOGGER.trace("Parsed {} unique admins", adminSteamIds.size());

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
                A2SInfoResponse info = a2sEvent.getResponse().getInfo();
                A2SRulesResponse rules = a2sEvent.getResponse().getRules();

                if (info == null || rules == null)
                    break;

                serverName = info.getName();
                maxPlayers = (int) info.getMaxPlayers();

                publicSlots = Integer.valueOf(rules.getRuleValue("NUMPUBCONN"));
                reserveSlots = Integer.valueOf(rules.getRuleValue("NUMPRIVCONN"));

                playerCount = Integer.valueOf(rules.getRuleValue("PlayerCount_i"));
                publicQueue = Integer.valueOf(rules.getRuleValue("PublicQueue_i"));
                reserveQueue = Integer.valueOf(rules.getRuleValue("ReservedQueue_i"));

                matchTimeout = Double.valueOf(rules.getRuleValue("MatchTimeout_f"));
                gameVersion = info.getVersion();

                LOGGER.trace("Done updating SquadServer A2S info");

                break;
            //Log Parser
            case NEW_GAME:
                LOGGER.trace("Updating SquadServer for NEW_GAME");
                NewGameEvent newGameEvent = (NewGameEvent) ev;
                maxTickRate = newGameEvent.getMaxTickRate();
                currentLayer = newGameEvent.getLayerName();
                A2SUpdater.updateA2S();
                RconUpdater.updatePlayerList();
                RconUpdater.updateSquadList();
                RconUpdater.updateLayerInfo();
                if (currentLayer.toLowerCase().contains("raas")) {
                    Rcon.command("AdminSetFogOfWar 0");
                }
                //TODO: Update admins since these can change between games
                LOGGER.trace("Done updating SquadServer for NEW_GAME");
                break;
            case PLAYER_CONNECTED:
                //TODO: Update admins if player connected has steam id in adminSteamIds
                break;
            case PLAYER_DISCONNECTED:
                LOGGER.trace("Updating SquadServer for PLAYER_DISCONNECTED");
                PlayerDisconnectedEvent playerDisconnectedEvent = (PlayerDisconnectedEvent) ev;
                nameSteamIds.inverse().remove(playerDisconnectedEvent.getSteamId());
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
                LOGGER.trace("Updating SquadServer for STEAMID_CONNECTED");
                SteamIdConnectedEvent steamidConnectedEvent = (SteamIdConnectedEvent) ev;
                nameSteamIds.put(steamidConnectedEvent.getName(), steamidConnectedEvent.getSteamId());
                if (!entityManager.isPlayerExist(steamidConnectedEvent.getSteamId())) {
                    RconUpdater.updatePlayerList();
                    OnlinePlayer onlinePlayer = onlinePlayers.stream()
                            .filter(onlPlayer -> onlPlayer.getSteamId() == steamidConnectedEvent.getSteamId())
                            .findFirst()
                            .orElseThrow();
                    entityManager.addPlayer(onlinePlayer.getSteamId(), onlinePlayer.getName());
                } else {
                    RconUpdater.updatePlayerList();
                    PlayerEntity playerEntity = entityManager.getPlayerBySteamId(steamidConnectedEvent.getSteamId());
                    OnlinePlayer onlinePlayer = onlinePlayers.stream()
                            .filter(onlPlayer -> onlPlayer.getSteamId() == steamidConnectedEvent.getSteamId())
                            .findFirst()
                            .orElseThrow();
                    if (!playerEntity.getName().equals(onlinePlayer.getName())) {
                        //LOGGER.info("\u001B[46m \u001B[30m (onSteamIdConnected) Player change name from '{}' to '{}' \u001B[0m", playerEntity.getName(), onlinePlayer.getName());
                        //String oldName = playerEntity.getName();
                        playerEntity.setName(onlinePlayer.getName());
                        entityManager.update(playerEntity);
                        //entityManager.addPlayerNote(playerEntity, "Игрок изменил имя с '" + oldName + "' на '" + playerEntity.getName() + "'");
                    }
                }
                LOGGER.trace("Done updating SquadServer for STEAMID_CONNECTED");
                break;
            //Rcon
            case CHAT_MESSAGE:
                ChatMessageEvent cme = (ChatMessageEvent) ev;
                entityManager.addPlayerMessage(cme.getSteamId(), cme.getChatType(), cme.getMessage());
                LOGGER.trace("'{}' send new message '{}' in chat '{}'", cme.getPlayerName(), cme.getMessage(), cme.getChatType());
                System.out.println();
                break;
            case LAYERINFO_UPDATED:
                LOGGER.trace("Updating SquadServer for LAYERINFO_UPDATED");
                currentMap = ((LayerInfoUpdatedEvent) ev).getCurrentMap();
                currentLayer = ((LayerInfoUpdatedEvent) ev).getCurrentLayer();
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
                onlinePlayers = playerListUpdatedEvent.getOnlinePlayersList();
                disconnectedPlayers = playerListUpdatedEvent.getDisconnectedPlayersList();
                LOGGER.trace("Done updating SquadServer for PLAYERLIST_UPDATED");
                break;
            case POSSESSED_ADMIN_CAM:
                LOGGER.trace("Updating SquadServer for POSSESSED_ADMIN_CAM");
                PossessedAdminCameraEvent possessedAdminCameraEvent = (PossessedAdminCameraEvent) ev;
                getPlayerBySteamId(possessedAdminCameraEvent.getSteamId()).ifPresent(p -> adminsInAdminCam.add(p));
                LOGGER.trace("Done updating SquadServer for POSSESSED_ADMIN_CAM");
                break;
            case SQUADLIST_UPDATED:
                LOGGER.trace("Updating SquadServer for SQUADLIST_UPDATED");
                SquadAndTeamListsUpdatedEvent squadAndTeamListsUpdatedEvent = (SquadAndTeamListsUpdatedEvent) ev;
                squads = squadAndTeamListsUpdatedEvent.getSquadList();
                teams = squadAndTeamListsUpdatedEvent.getTeamsList();
                LOGGER.trace("Done updating SquadServer for SQUADLIST_UPDATED");
                break;
            case UNPOSSESSED_ADMIN_CAM:
                LOGGER.trace("Updating SquadServer for UNPOSSESSED_ADMIN_CAM");
                UnpossessedAdminCameraEvent unpossessedAdminCameraEvent = (UnpossessedAdminCameraEvent) ev;
                getPlayerBySteamId(unpossessedAdminCameraEvent.getSteamId()).ifPresent(p -> adminsInAdminCam.remove(p));
                LOGGER.trace("Done updating SquadServer for UNPOSSESSED_ADMIN_CAM");
                break;
            default:
                LOGGER.trace("SquadServer received non-supported event. Ignoring.");
        }
    }

    public static OnlineInfo getTeamsWithSquadsAndPlayers() {
        OnlineInfo onlineInfo = new OnlineInfo();
        getTeams().forEach(team -> onlineInfo.addTeam(SerializationUtils.clone(team)));
        getSquads().forEach(squad -> onlineInfo.getTeamById(squad.getTeamId()).addSquad(SerializationUtils.clone(squad)));
        getDisconnectedPlayers().forEach(disconnectedPlayer -> onlineInfo.addDisconnectedPlayer(SerializationUtils.clone(disconnectedPlayer)));

        getOnlinePlayers().forEach(onlinePlayer -> {
            if (onlinePlayer.getSquadID() == null) {
                onlineInfo.getTeamById(onlinePlayer.getTeamId()).addPlayerWithoutSquad(SerializationUtils.clone(onlinePlayer));
            } else {
                onlineInfo.getTeamById(onlinePlayer.getTeamId()).getSquadById(onlinePlayer.getSquadID()).addPlayer(SerializationUtils.clone(onlinePlayer));
            }
        });
        return onlineInfo;
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

    public static Collection<OnlinePlayer> getAdmins() {
        return Collections.unmodifiableCollection(admins);
    }

    public static Collection<OnlinePlayer> getAdminsInAdminCam() {
        return Collections.unmodifiableCollection(adminsInAdminCam);
    }
}
