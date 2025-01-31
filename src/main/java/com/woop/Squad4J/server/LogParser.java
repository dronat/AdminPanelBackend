package com.woop.Squad4J.server;

import com.example.adminpanelbackend.SteamService;
import com.woop.Squad4J.event.Event;
import com.woop.Squad4J.event.EventType;
import com.woop.Squad4J.event.logparser.*;
import com.woop.Squad4J.event.rcon.ChatMessageEvent;
import com.woop.Squad4J.event.rcon.PlayerBannedEvent;
import com.woop.Squad4J.event.rcon.PlayerKickedEvent;
import com.woop.Squad4J.event.rcon.PlayerWarnedEvent;
import com.woop.Squad4J.server.tailer.LogTailer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class whose responsibility is parsing lines passed to it from {@link LogTailer}.
 * <p>
 * Parsing lines involves determining what type of {@link Event} a specific line should emit, creating said event,
 * and passing it to the {@link EventEmitter}, which is responsible for passing the event to the {@link SquadServer}
 * and to any plugins which are event-bound.
 *
 * @author Robert Engle
 */
public class LogParser {
    private static final Map<Pattern, EventType> logPatterns = new HashMap<>();
    private static final Logger LOGGER = LoggerFactory.getLogger(LogParser.class);

    private static final RestTemplate restTemplate = new RestTemplate();

    static {
        logPatterns.put(Pattern.compile("^\\[([0-9.:-]+)\\]\\[([ 0-9]*)\\]LogSquad: ADMIN COMMAND: Message broadcasted <(.+)> from (.+)"), EventType.ADMIN_BROADCAST);
        logPatterns.put(Pattern.compile("^\\[([0-9.:-]+)]\\[([ 0-9]*)]LogSquadTrace: \\[DedicatedServer](?:ASQDeployable::)?TakeDamage\\(\\): ([A-z0-9_]+)_C_[0-9]+: ([0-9.]+) damage attempt by causer ([A-z0-9_]+)_C_[0-9]+ instigator (.+) with damage type ([A-z0-9_]+)_C health remaining ([0-9.]+)"), EventType.DEPLOYABLE_DAMAGED);
        logPatterns.put(Pattern.compile("^\\[([0-9.:-]+)]\\[([ 0-9]*)]LogWorld: Bringing World \\/([A-z]+)\\/(?:Maps\\/)?([A-z0-9-]+)\\/(?:.+\\/)?([A-z0-9-]+)(?:\\.[A-z0-9-]+) up for play \\(max tick rate ([0-9]+)\\) at ([.0-9]+)-([.0-9]+)"), EventType.NEW_GAME);
        logPatterns.put(Pattern.compile("^\\[([0-9.:-]+)]\\[([ 0-9]*)]LogNet: Join succeeded: (.+)"), EventType.PLAYER_CONNECTED);
        logPatterns.put(Pattern.compile("^\\[([0-9.:-]+)]\\[([ 0-9]*)]LogSquad: Player:(.+) ActualDamage=([0-9.]+) from (.+) caused by ([A-z_0-9]+)_C_([0-9]+)"), EventType.PLAYER_DAMAGED);
        logPatterns.put(Pattern.compile("^\\[([0-9.:-]+)]\\[([ 0-9]*)]LogSquadTrace: \\[DedicatedServer](?:ASQSoldier::)?Die\\(\\): Player:(.+) KillingDamage=(?:-)*([0-9.]+) from ([A-z_0-9]+) caused by ([A-z_0-9]+)_C_([0-9]+)"), EventType.PLAYER_DIED);
        logPatterns.put(Pattern.compile("^\\[([0-9.:-]+)]\\[([ 0-9]*)]LogEasyAntiCheatServer: \\[[0-9:]+] \\[[A-z]+] \\[EAC Server] \\[Info] \\[UnregisterClient] Client: ([A-z0-9]+) PlayerGUID: ([0-9]{17})"), EventType.PLAYER_DISCONNECTED);
        logPatterns.put(Pattern.compile("^\\[([0-9.:-]+)]\\[([ 0-9]*)]LogSquadTrace: \\[DedicatedServer](?:ASQPlayerController::)?OnPossess\\(\\): PC=(.+) Pawn=([A-z0-9_]+)_C_([0-9]+) FullPath=(?:[A-Za-z0-9._]+) (?:[A-Za-z0-9./_:]+)"), EventType.PLAYER_POSSESS);
        logPatterns.put(Pattern.compile("\\[([0-9.:-]+)]\\[([ 0-9]*)]LogSquad: (.+) has revived (.+)\\."), EventType.PLAYER_REVIVED);
        logPatterns.put(Pattern.compile("\\[([0-9.:-]+)]\\[([ 0-9]*)]LogSquadTrace: \\[DedicatedServer](?:ASQPlayerController::)?OnUnPossess\\(\\): PC=(.+)"), EventType.PLAYER_UNPOSSESS);
        logPatterns.put(Pattern.compile("^\\[([0-9.:-]+)]\\[([ 0-9]*)]LogSquadTrace: \\[DedicatedServer](?:ASQSoldier::)?Wound\\(\\): Player:(.+) KillingDamage=(?:-)*([0-9.]+) from ([A-z_0-9]+) caused by ([A-z_0-9]+)_C_([0-9]+)"), EventType.PLAYER_WOUNDED);
        logPatterns.put(Pattern.compile("^\\[([0-9.:-]+)]\\[([ 0-9]*)]LogSquadTrace: \\[DedicatedServer](?:ASQGameMode::)?DetermineMatchWinner\\(\\): (.+) won on (.+)"), EventType.ROUND_WINNER);
        logPatterns.put(Pattern.compile("^\\[([0-9.:-]+)]\\[([ 0-9]*)]LogSquad: USQGameState: Server Tick Rate: ([0-9.]+)"), EventType.SERVER_TICK_RATE);
        logPatterns.put(Pattern.compile("^\\[([0-9.:-]+)]\\[([ 0-9]*)]LogSquad: (.+) \\(Steam ID: ([0-9]{17})\\) has created Squad (\\d+) \\(Squad Name: (.+)\\) on (.+)"), EventType.SQUAD_CREATED);
        //logPatterns.put(Pattern.compile("^\\[([0-9.:-]+)]\\[([ 0-9]*)]LogEasyAntiCheatServer: \\[[0-9:]+] \\[[A-z]+] \\[EAC Server] \\[Info] \\[RegisterClient] Client: (?:[A-z0-9]+) PlayerGUID: ([0-9]{17}) PlayerIP: [0-9]{17} OwnerGUID: [0-9]{17} PlayerName: (.+)"), EventType.STEAMID_CONNECTED);
        logPatterns.put(Pattern.compile("^\\[([0-9.:-]+)]\\[([ 0-9]*)]LogOnline: STEAM: AUTH HANDLER: Sending auth result to user (\\d{17}) with flag success\\? 1"), EventType.STEAMID_CONNECTED);
        logPatterns.put(Pattern.compile("\\[(ChatAll|ChatTeam|ChatSquad|ChatAdmin)] \\[SteamID:([0-9]{17})] (.+?) : (.*)"), EventType.CHAT_MESSAGE);
        //logPatterns.put(Pattern.compile("\\[SteamID:([0-9]{17})] (.+?) has possessed admin camera."), EventType.ENTERED_IN_ADMIN_CAM);
        //logPatterns.put(Pattern.compile("\\[SteamID:([0-9]{17})] (.+?) has unpossessed admin camera."), EventType.LEFT_FROM_ADMIN_CAM);
        logPatterns.put(Pattern.compile("Remote admin has warned player (.*)\\. Message was \"(.*)\""), EventType.PLAYER_WARNED);
        logPatterns.put(Pattern.compile("Kicked player ([0-9]+)\\. \\[steamid=([0-9]{17})] (.*)"), EventType.PLAYER_KICKED);
        logPatterns.put(Pattern.compile("Banned player ([0-9]+)\\. \\[steamid=(.*?)\\] (.*) for interval (.*)"), EventType.PLAYER_BANNED);
    }

    private LogParser() {
        throw new UnsupportedOperationException("You cannot instantiate this class.");
    }

    public static void parseLine(String line) {
        LOGGER.trace("Received {}", line);

        AtomicReference<Event> event = new AtomicReference<>(null);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy.MM.dd-HH.mm.ss:SSS");

        logPatterns.forEach((pattern, type) -> {
            Matcher matcher = pattern.matcher(line);
            if (matcher.find()) {
                try {
                    switch (type) {
                        case ADMIN_BROADCAST:
                            event.set(new AdminBroadcastEvent(
                                    formatter.parse(matcher.group(1)),
                                    type,
                                    Integer.parseInt(matcher.group(2).strip()),
                                    matcher.group(3),
                                    matcher.group(4)
                            ));
                            break;
                        case DEPLOYABLE_DAMAGED:
                            event.set(new DeployableDamagedEvent(
                                    formatter.parse(matcher.group(1)),
                                    type,
                                    Integer.parseInt(matcher.group(2).strip()),
                                    matcher.group(3),
                                    Double.parseDouble(matcher.group(4)),
                                    matcher.group(5),
                                    matcher.group(6),
                                    matcher.group(7),
                                    Double.parseDouble(matcher.group(8))
                            ));
                            break;
                        case NEW_GAME:
                            if (matcher.group(5).equalsIgnoreCase("TransitionMap")) {
                                break;
                            }
                            event.set(new NewGameEvent(
                                    formatter.parse(matcher.group(1)),
                                    type,
                                    Integer.parseInt(matcher.group(2).strip()),
                                    matcher.group(3),
                                    matcher.group(4),
                                    matcher.group(5),
                                    Integer.parseInt(matcher.group(6))
                            ));
                            break;
                        case PLAYER_CONNECTED:
                            event.set(new PlayerConnectedEvent(
                                    formatter.parse(matcher.group(1)),
                                    type,
                                    Integer.parseInt(matcher.group(2).strip()),
                                    matcher.group(3)
                            ));
                            break;
                        case PLAYER_DAMAGED:
                            event.set(new PlayerDamagedEvent(
                                    formatter.parse(matcher.group(1)),
                                    type,
                                    Integer.parseInt(matcher.group(2).strip()),
                                    matcher.group(3),
                                    Double.parseDouble(matcher.group(4)),
                                    matcher.group(5),
                                    matcher.group(6),
                                    matcher.group(7)
                            ));
                            break;
                        case PLAYER_DIED:
                            event.set(new PlayerDiedEvent(
                                    formatter.parse(matcher.group(1)),
                                    type,
                                    Integer.parseInt(matcher.group(2).strip()),
                                    matcher.group(3),
                                    Double.parseDouble(matcher.group(4)),
                                    matcher.group(5),
                                    matcher.group(6)
                            ));
                            break;
                        case PLAYER_DISCONNECTED:
                            event.set(new PlayerDisconnectedEvent(
                                    formatter.parse(matcher.group(1)),
                                    type,
                                    Integer.parseInt(matcher.group(2).strip()),
                                    Long.parseLong(matcher.group(4))
                            ));
                            break;
                        /*case ENTERED_IN_ADMIN_CAM:
                            event.set(new EnteredInAdminCam(
                                    formatter.parse(matcher.group(1)),
                                    type,
                                    Integer.parseInt(matcher.group(2).strip()),
                                    matcher.group(3),
                                    matcher.group(4)
                            ));
                            break;*/
                        case PLAYER_REVIVED:
                            event.set(new PlayerRevivedEvent(
                                    formatter.parse(matcher.group(1)),
                                    type,
                                    Integer.parseInt(matcher.group(2).strip()),
                                    matcher.group(3),
                                    matcher.group(4)
                            ));
                            break;
                        /*case LEFT_FROM_ADMIN_CAM:
                            event.set(new LeftFromAdminCam(
                                    formatter.parse(matcher.group(1)),
                                    type,
                                    Integer.parseInt(matcher.group(2).strip()),
                                    matcher.group(3)
                            ));
                            break;*/
                        case PLAYER_WOUNDED:
                            event.set(new PlayerWoundedEvent(
                                    formatter.parse(matcher.group(1)),
                                    type,
                                    Integer.parseInt(matcher.group(2).strip()),
                                    matcher.group(3),
                                    Double.parseDouble(matcher.group(4)),
                                    matcher.group(5),
                                    matcher.group(6)
                            ));
                            break;
                        case ROUND_WINNER:
                            event.set(new RoundWinnerEvent(
                                    formatter.parse(matcher.group(1)),
                                    type,
                                    Integer.parseInt(matcher.group(2).strip()),
                                    matcher.group(3),
                                    matcher.group(4)
                            ));
                            break;
                        case SERVER_TICK_RATE:
                            event.set(new ServerTickRateEvent(
                                    formatter.parse(matcher.group(1)),
                                    type,
                                    Integer.parseInt(matcher.group(2).strip()),
                                    Double.parseDouble(matcher.group(3))
                            ));
                            break;
                        case SQUAD_CREATED:
                            event.set(new SquadCreatedEvent(
                                    formatter.parse(matcher.group(1)),
                                    type,
                                    Integer.parseInt(matcher.group(2).strip()),
                                    matcher.group(3),
                                    Long.parseLong(matcher.group(4)),
                                    Integer.parseInt(matcher.group(5)),
                                    matcher.group(6),
                                    matcher.group(7)
                            ));
                            break;
                        case STEAMID_CONNECTED:
                            long steamId = Long.parseLong(matcher.group(3));
                            event.set(new SteamIdConnectedEvent(
                                    formatter.parse(matcher.group(1)),
                                    type,
                                    Integer.parseInt(matcher.group(2).strip()),
                                    steamId,
                                    SteamService.getSteamUserInfo(steamId).getPersonaname()
                            ));
                            break;
                        case CHAT_MESSAGE:
                            event.set(new ChatMessageEvent(
                                    new Date(),
                                    type,
                                    matcher.group(1),
                                    Long.parseLong(matcher.group(2)),
                                    matcher.group(3),
                                    matcher.group(4)
                            ));
                            break;
                        case PLAYER_BANNED:
                            event.set(new PlayerBannedEvent(
                                    new Date(),
                                    type,
                                    Long.parseLong(matcher.group(1)),
                                    Long.parseLong(matcher.group(2)),
                                    matcher.group(3),
                                    matcher.group(4)
                            ));
                            break;
                        case PLAYER_KICKED:
                            event.set(new PlayerKickedEvent(
                                    new Date(),
                                    type,
                                    Long.parseLong(matcher.group(1)),
                                    Long.parseLong(matcher.group(2)),
                                    matcher.group(3)
                            ));
                            break;
                        case PLAYER_WARNED:
                            event.set(new PlayerWarnedEvent(
                                    new Date(),
                                    type,
                                    matcher.group(1),
                                    matcher.group(2)
                            ));
                            break;
                        default:
                            LOGGER.trace("Unrecognized event passed to LogParser, ignoring.");
                    }
                } catch (ParseException e) {
                    LOGGER.error("Error parsing date.");
                    LOGGER.error(e.getMessage());
                }
            }
        }); //end logPatterns forEach

        if (event.get() != null) {
            EventEmitter.emit(event.get());
        }

    }//end parseLine method
}//end LogParser class
