package com.woop.Squad4J.server;

import com.woop.Squad4J.concurrent.GlobalThreadPool;
import com.woop.Squad4J.event.Event;
import com.woop.Squad4J.event.EventType;
import com.woop.Squad4J.event.rcon.LayerInfoUpdatedEvent;
import com.woop.Squad4J.event.rcon.PlayerListUpdatedEvent;
import com.woop.Squad4J.event.rcon.SquadAndTeamListsUpdatedEvent;
import com.woop.Squad4J.model.DisconnectedPlayer;
import com.woop.Squad4J.model.OnlinePlayer;
import com.woop.Squad4J.model.Squad;
import com.woop.Squad4J.model.Team;
import com.woop.Squad4J.rcon.Rcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class which has the responsibility of updating information for {@link SquadServer} that is obtained through the
 * RCON console.
 *
 * @author Robert Engle
 */
public class RconUpdater {
    private static final Logger LOGGER = LoggerFactory.getLogger(RconUpdater.class);

    private static boolean initialized = false;

    private static final Pattern onlinePlayerPattern = Pattern.compile("ID: ([0-9]+) \\| SteamID: ([0-9]+) \\| Name: (.+) \\| Team ID: (1|2|N/A) \\| Squad ID: ([0-9]+|N/A) \\| Is Leader: (True|False) \\| Role: (.+)");
    private static final Pattern disconnectedPlayerPattern = Pattern.compile("ID: ([0-9]+) \\| SteamID: ([0-9]+) \\| Since Disconnect: (.+) \\| Name: (.+)");
    private static final Pattern squadPattern = Pattern.compile("ID: ([0-9]+) \\| Name: (.+) \\| Size: ([0-9]+) \\| Locked: (True|False) \\| Creator Name: (.+) \\| Creator Steam ID: ([0-9]{17})");
    private static final Pattern currentLayerPattern = Pattern.compile("Current level is (.+), layer is (.+)");
    private static final Pattern nextLayerPattern = Pattern.compile("Next level is (.+), layer is (.+)");

    private RconUpdater(){
        throw new IllegalStateException("This class cannot be instantiated.");
    }

    public static void init(){
        if(initialized)
            throw new IllegalStateException(RconUpdater.class.getSimpleName() + " has already been initialized.");

        GlobalThreadPool.getScheduler().scheduleWithFixedDelay(RconUpdater::updateRcon, 5, 1, TimeUnit.SECONDS);

        initialized = true;
    }

    /**
     * Helper method to update information retrieved through RCON: player list, squad list, and layer info.
     */
    public static void updateRcon() {
        updateSquadList();
        updateLayerInfo();
        updatePlayerList();
        LOGGER.info("Rcon updated");
    }

    /**
     * Updates the player list by querying the RCON console for a player list.
     */
    protected static void updatePlayerList(){
        //long b = System.currentTimeMillis();
        LOGGER.trace("Retrieving player list.");
        //System.out.println("ListPlayers");
        String response = Rcon.command("ListPlayers");
        //System.out.println(response);
        //System.out.println("GetPlayerList: " + (System.currentTimeMillis() - b));
        long a = System.currentTimeMillis();
        List<OnlinePlayer> onlineOnlinePlayers = new ArrayList<>();
        List<DisconnectedPlayer> disconnectedPlayers = new ArrayList<>();
        StringTokenizer tokenizer = new StringTokenizer(response, "\n");
        while(tokenizer.hasMoreElements()){
            String line = tokenizer.nextToken();
            Matcher onlineMatcher = onlinePlayerPattern.matcher(line);
            Matcher disconnectedMatcher = disconnectedPlayerPattern.matcher(line);
            if(onlineMatcher.find()){
                Integer id = Integer.valueOf(onlineMatcher.group(1));
                long steamId = Long.parseLong(onlineMatcher.group(2));
                String name = onlineMatcher.group(3);
                Integer teamId = onlineMatcher.group(4).equals("N/A") ? null : Integer.valueOf(onlineMatcher.group(4));
                Integer squadId = onlineMatcher.group(5).equals("N/A") ? null : Integer.valueOf(onlineMatcher.group(5));
                Boolean isLeader = Boolean.valueOf(onlineMatcher.group(6));
                String role = onlineMatcher.group(7);

                OnlinePlayer onlinePlayer = new OnlinePlayer(id, steamId, name, teamId, squadId, isLeader, role);
                onlineOnlinePlayers.add(onlinePlayer);
            } else if (disconnectedMatcher.find()) {
                Integer id = Integer.valueOf(disconnectedMatcher.group(1));
                long steamId = Long.parseLong(disconnectedMatcher.group(2));
                String sinceDisconnect = disconnectedMatcher.group(3);
                String name = disconnectedMatcher.group(4);

                DisconnectedPlayer disconnectedPlayer = new DisconnectedPlayer(id, steamId, sinceDisconnect, name);
                disconnectedPlayers.add(disconnectedPlayer);
            }
        }
        LOGGER.trace("Retrieved {} onlinePlayers.", onlineOnlinePlayers.size());
        LOGGER.trace("Retrieved {} disconnected Players.", disconnectedPlayers.size());

        Event event = new PlayerListUpdatedEvent(new Date(), EventType.PLAYERLIST_UPDATED, onlineOnlinePlayers, disconnectedPlayers);
        //System.out.println("SquadsAndTeamsResponse: \n" + response);

        EventEmitter.emit(event);
        //System.out.println("ParsePlayerList: " + (System.currentTimeMillis() - a));
    }

    /**
     * Updates the squad list by querying the RCON console.
     */
    protected static void updateSquadList(){
        long b = System.currentTimeMillis();
        LOGGER.trace("Retrieving squad list.");
        //System.out.println("ListSquads");
        String response = Rcon.command("ListSquads");
        //System.out.println(response);
        //System.out.println("GetSquadList: " + (System.currentTimeMillis() - b));
        long a = System.currentTimeMillis();
        List<Squad> squads = new ArrayList<>();
        List<Team> teams = new ArrayList<>();
        int teamId = 1;
        StringTokenizer tokenizer = new StringTokenizer(response, "\n");
        while(tokenizer.hasMoreElements()){
            String line = tokenizer.nextToken();
            if (line.startsWith("Team ID:")) {
                String teamName = line.substring(line.indexOf("(") + 1, line.indexOf(")"));
                teams.add(new Team(teamName.trim(), line.startsWith("Team ID: 1") ? 1 : 2));
            }
            if(line.startsWith("Team ID: 2")){
                teamId = 2;
                continue;
            }

            Matcher matcher = squadPattern.matcher(line);
            if(matcher.find()){
                Integer id = Integer.valueOf(matcher.group(1));
                String name = matcher.group(2);
                Integer size = Integer.valueOf(matcher.group(3));
                Boolean isLocked = Boolean.valueOf(matcher.group(4));
                String creatorName = matcher.group(5);
                String creatorSteam64id = matcher.group(6);

                Squad squad = new Squad(teamId, id, name, size, isLocked, creatorName, creatorSteam64id);
                squads.add(squad);
            }
        }
        LOGGER.trace("Retrieved {} squads.", squads.size());

        Event event = new SquadAndTeamListsUpdatedEvent(new Date(), EventType.SQUADLIST_UPDATED, squads, teams);
        EventEmitter.emit(event);
        //System.out.println("ParseSquadList: " + (System.currentTimeMillis() - a));
    }

    /**
     * Updates the layer information by querying the RCON console.
     *
     * Updates both the current and next layers/maps.
     */
    public static void updateLayerInfo(){
        LOGGER.trace("Retrieving layer information");
        String currentLayer = "";
        String nextLayer = "";
        String currentMap = "";
        String nextMap = "";
        //System.out.println("ShowCurrentMap");
        String response = Rcon.command("ShowCurrentMap");
        //System.out.println(response);
        LOGGER.trace("Getting current map. Response: {}", response);
        Matcher matcher = currentLayerPattern.matcher(response);
        if(matcher.find()){
            currentMap = matcher.group(1);
            currentLayer = matcher.group(2);
            LOGGER.trace("Current layer is {}", currentLayer);
        }

        response = Rcon.command("ShowNextMap");
        LOGGER.trace("Retrieved next map. Response: {}", response);
        matcher = nextLayerPattern.matcher(response);
        if(matcher.find()){
            nextMap = matcher.group(1);
            nextLayer = matcher.group(2);
            LOGGER.trace("Next layer is {}", nextLayer);
        }

        Event event = new LayerInfoUpdatedEvent(new Date(), EventType.LAYERINFO_UPDATED, currentMap, currentLayer, nextMap, nextLayer);

        LOGGER.trace("Retrieved layer information");
        EventEmitter.emit(event);
    }
}