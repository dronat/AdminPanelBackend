package com.example.adminpanelbackend.controller;

import com.example.adminpanelbackend.SteamService;
import com.example.adminpanelbackend.dataBase.EntityManager;
import com.example.adminpanelbackend.dataBase.entity.*;
import com.example.adminpanelbackend.dataBase.service.AdminActionLogsService;
import com.example.adminpanelbackend.dataBase.service.AdminService;
import com.example.adminpanelbackend.dataBase.service.PlayerEntityService;
import com.example.adminpanelbackend.dataBase.service.PlayerBanService;
import com.example.adminpanelbackend.model.SteamUserModel;
import com.woop.Squad4J.event.rcon.ChatMessageEvent;
import com.woop.Squad4J.model.DisconnectedPlayer;
import com.woop.Squad4J.model.OnlineInfo;
import com.woop.Squad4J.model.OnlinePlayer;
import com.woop.Squad4J.rcon.Rcon;
import com.woop.Squad4J.server.RconUpdater;
import com.woop.Squad4J.server.SquadServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;
import org.springframework.session.jdbc.config.annotation.web.http.EnableJdbcHttpSession;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.sql.Timestamp;
import java.util.*;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@RestController()
@EnableJdbcHttpSession(maxInactiveIntervalInSeconds = 604800)
@CrossOrigin
//@SessionAttributes("userInfo")
public class SecureController {
    private static final Logger LOGGER = LoggerFactory.getLogger(NotSecureController.class);
    EntityManager entityManager = new EntityManager();
    @Autowired
    PlayerEntityService playerEntityService;

    @Autowired
    AdminActionLogsService adminActionLogsService;
    @Autowired
    PlayerBanService playerBanService;

    @Autowired
    AdminService adminService;

    @Autowired
    FindByIndexNameSessionRepository<? extends Session> sessions;

    @GetMapping(path = "/get-online-players")
    public ResponseEntity<OnlineInfo> getOnline(@SessionAttribute AdminEntity userInfo, HttpSession httpSession, HttpServletRequest request, HttpServletResponse response) {
        LOGGER.debug("Received secured GET request on '{}' with userInfo in cookie '{}'", request.getRequestURL(), userInfo);
        return ResponseEntity.ok(SquadServer.getOnlineTeamsWithSquadsAndPlayers());
    }

    @GetMapping(path = "/get-chat-messages")
    public ResponseEntity<Collection<ChatMessageEvent>> getChatMessages(@SessionAttribute AdminEntity userInfo, HttpSession httpSession, HttpServletRequest request, HttpServletResponse response) {
        LOGGER.debug("Received secured GET request on '{}' with userInfo in cookie '{}'", request.getRequestURL(), userInfo);
        return ResponseEntity.ok(SquadServer.getChatMessages());
    }

    @GetMapping(path = "/get-disconnected-players")
    public ResponseEntity<Collection<DisconnectedPlayer>> getDisconnectedPlayers(@SessionAttribute AdminEntity userInfo, HttpSession httpSession, HttpServletRequest request, HttpServletResponse response) {
        LOGGER.debug("Received secured GET request on '{}' with userInfo in cookie '{}'", request.getRequestURL(), userInfo);
        return ResponseEntity.ok(SquadServer.getDisconnectedPlayers());
    }

    @GetMapping(path = "/get-server-info")
    public ResponseEntity<HashMap<String, Object>> getServerInfo(@SessionAttribute AdminEntity userInfo, HttpSession httpSession, HttpServletRequest request, HttpServletResponse response) {
        LOGGER.debug("Received secured GET request on '{}' with userInfo in cookie '{}'", request.getRequestURL(), userInfo);
        return ResponseEntity.ok(SquadServer.getServerInfo());
    }

    @PostMapping(path = "/add-admin")
    public ResponseEntity<Void> addAdmin(@SessionAttribute AdminEntity userInfo, HttpSession httpSession, HttpServletRequest request, HttpServletResponse response, @RequestParam long adminSteamId) {
        LOGGER.debug("Received secured {} request on '{}' with userInfo in cookie '{}'", request.getMethod(), request.getRequestURL(), userInfo);
        entityManager.addAdmin(adminSteamId);
        entityManager.addAdminActionInLog(userInfo.getSteamId(), null, "AddAdmin", String.valueOf(adminSteamId));
        return ResponseEntity.ok().build();
    }

    @PostMapping(path = "/deactivate-admin")
    public ResponseEntity<OnlineInfo> deactivateAdmin(@SessionAttribute AdminEntity userInfo, HttpSession httpSession, HttpServletRequest request, HttpServletResponse response, @RequestParam long adminSteamId) {
        LOGGER.debug("Received secured {} request on '{}' with userInfo in cookie '{}'", request.getMethod(), request.getRequestURL(), userInfo);
        if (userInfo.getSteamId() != 76561198069397368L) {
            return ResponseEntity.status(403).build();
        }
        entityManager.deactivateAdmin(adminSteamId);
        entityManager.addAdminActionInLog(userInfo.getSteamId(), null, "DeactivateAdmin", String.valueOf(adminSteamId));
        Map<String, ? extends Session> resultSessions = sessions.findByPrincipalName(String.valueOf(adminSteamId));
        resultSessions.forEach((k, v) -> {
            sessions.deleteById(v.getId());
        });
        if (!resultSessions.isEmpty()) {
            return ResponseEntity.status(BAD_REQUEST).build();
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping(path = "/add-player")
    public ResponseEntity<HashMap<String, Object>> addPlayer(@SessionAttribute AdminEntity userInfo, HttpSession httpSession, HttpServletRequest request, HttpServletResponse response, @RequestParam long steamId) {
        LOGGER.debug("Received secured {} request on '{}' with userInfo in cookie '{}'", request.getMethod(), request.getRequestURL(), userInfo);
        PlayerEntity player = entityManager.getPlayerBySteamId(steamId);
        if (player == null) {
            return ResponseEntity.status(BAD_REQUEST).build();
        }
        SteamUserModel.Response.Player steamUser = SteamService.getSteamUserInfo(steamId);
        entityManager.addPlayer(steamId, steamUser.getPersonaname());
        entityManager.addAdminActionInLog(userInfo.getSteamId(), steamId, "AddNewPlayer", null);
        OnlinePlayer onlinePlayer = SquadServer.getOnlinePlayers().stream().filter(elm -> Objects.equals(elm.getSteamId(), player.getSteamId())).findFirst().orElse(null);
        return ResponseEntity.ok(
                new HashMap<>() {{
                    put("name", player.getName());
                    put("steamId", player.getSteamId());
                    put("isOnline", onlinePlayer);
                    put("isOnControl", player.getOnControl());
                    put("isAdmin", SquadServer.getAdmins().contains(steamId));
                    put("avatarFull", steamUser.getAvatarfull());
                    put("numOfActiveBans", player
                            .getPlayersBansBySteamId()
                            .stream()
                            .filter(ban -> !ban.getIsUnbannedManually() && (ban.getExpirationTime() == null || ban.getExpirationTime().after(new Date(System.currentTimeMillis()))))
                            .count()
                    );
                }}
        );
    }

    @PostMapping(path = "/get-players")
    public ResponseEntity<HashMap<String, Object>> getPlayers(@SessionAttribute AdminEntity userInfo, HttpSession httpSession, HttpServletRequest request, HttpServletResponse response, @RequestParam int page, @RequestParam int size) {
        LOGGER.debug("Received secured {} request on '{}' with userInfo in cookie '{}'", request.getMethod(), request.getRequestURL(), userInfo);
        if (size > 100) {
            return ResponseEntity.status(BAD_REQUEST).build();
        }
        Page<PlayerEntity> resultPage = playerEntityService.findAll(PageRequest.of(page, size));
        HashMap<String, Object> map = new HashMap<>() {{
            put("currentPage", resultPage.getNumber());
            put("totalPages", resultPage.getTotalPages());
            put("totalElements", resultPage.getTotalElements());
            put("hasNext", resultPage.hasNext());
            put("nextPage", resultPage.hasNext() ? resultPage.nextPageable().getPageNumber() : null);
            put("hasPrevious", resultPage.hasPrevious());
            put("previousPage", resultPage.hasPrevious() ? resultPage.previousPageable().getPageNumber() : null);
            put("content", resultPage.getContent());
        }};

        List<HashMap<String, Object>> contentList = new ArrayList<>();

        resultPage.getContent().forEach(player -> contentList.add(new HashMap<>() {{
            put("steamId", player.getSteamId());
            put("name", player.getName());
            put("createTime", player.getCreateTime());
            put("playersBansBySteamId", player.getPlayersBansBySteamId().size());
            put("playersMessagesBySteamId", player.getPlayersMessagesBySteamId().size());
            put("playersNotesBySteamId", player.getPlayersNotesBySteamId().size());
            put("playersKicksBySteamId", player.getPlayersKicksBySteamId().size());
        }}));
        map.put("content", contentList);
        return ResponseEntity.ok(map);
    }

    @PostMapping(path = "/get-player")
    public ResponseEntity<HashMap<String, Object>> getPlayer(@SessionAttribute AdminEntity userInfo, HttpSession httpSession, HttpServletRequest request, HttpServletResponse response, @RequestParam long steamId) {
        LOGGER.debug("Received secured {} request on '{}' with userInfo in cookie '{}'", request.getMethod(), request.getRequestURL(), userInfo);
        PlayerEntity player = entityManager.getPlayerBySteamId(steamId);
        if (player == null) {
            return ResponseEntity.status(404).build();
        }
        OnlinePlayer onlinePlayer = SquadServer.getOnlinePlayers().stream().filter(elm -> Objects.equals(elm.getSteamId(), player.getSteamId())).findFirst().orElse(null);

        return ResponseEntity.ok(new HashMap<>() {{
            put("name", player.getName());
            put("steamId", player.getSteamId());
            put("isOnline", onlinePlayer);
            put("isOnControl", player.getOnControl());
            put("isAdmin", SquadServer.getAdmins().contains(steamId));
            put("avatarFull", SteamService.getSteamUserInfo(steamId).getAvatarfull());
            put("numOfActiveBans", player
                    .getPlayersBansBySteamId()
                    .stream()
                    .filter(ban -> !ban.getIsUnbannedManually() && (ban.getExpirationTime() == null || ban.getExpirationTime().after(new Date(System.currentTimeMillis()))))
                    .count()
            );
        }});
    }

    @PostMapping(path = "/get-players-by-contains-text")
    public ResponseEntity<HashMap<String, Object>> getPlayersByContainsText(@SessionAttribute AdminEntity userInfo, HttpSession httpSession, HttpServletRequest request, HttpServletResponse response, @RequestParam int maxSize, @RequestParam String text) {
        LOGGER.debug("Received secured {} request on '{}' with userInfo in cookie '{}'", request.getMethod(), request.getRequestURL(), userInfo);
        if (maxSize > 10) {
            return ResponseEntity.status(BAD_REQUEST).build();
        }
        HashMap<String, Object> resultMap = new HashMap<>();
        try {
            if (text.length() == 17) {
                Long.parseLong(text);
                resultMap.put("foundPlayers", playerEntityService.findAllBySteamIdContains(text, PageRequest.of(0, maxSize)).getContent());
            } else {
                resultMap.put("foundPlayers", playerEntityService.findAllByNameContainsIgnoreCase(text, PageRequest.of(0, maxSize)).getContent());
            }
        } catch (NumberFormatException e) {
            resultMap.put("foundPlayers", playerEntityService.findAllByNameContainsIgnoreCase(text, PageRequest.of(0, maxSize)).getContent());
        }
        return ResponseEntity.ok(resultMap);
    }

    @PostMapping(path = "/add-player-on-control")
    public ResponseEntity<Void> addPlayerOnControl(@SessionAttribute AdminEntity userInfo, HttpSession httpSession, HttpServletRequest request, HttpServletResponse response, @RequestParam long playerSteamId) {
        LOGGER.debug("Received secured {} request on '{}' with userInfo in cookie '{}'", request.getMethod(), request.getRequestURL(), userInfo);
        entityManager.addPlayerOnControl(userInfo.getSteamId(), playerSteamId);
        LOGGER.info("Admin '{}' add player '{}' on control", userInfo.getName(), playerSteamId);
        return ResponseEntity.ok().build();
    }

    @PostMapping(path = "/remove-player-from-control")
    public ResponseEntity<Void> removePlayerFromControl(@SessionAttribute AdminEntity userInfo, HttpSession httpSession, HttpServletRequest request, HttpServletResponse response, @RequestParam long playerSteamId) {
        LOGGER.debug("Received secured {} request on '{}' with userInfo in cookie '{}'", request.getMethod(), request.getRequestURL(), userInfo);
        entityManager.removePlayerFromControl(userInfo.getSteamId(), playerSteamId);
        LOGGER.info("Admin '{}' remove player '{}' from control", userInfo.getName(), playerSteamId);
        return ResponseEntity.ok().build();
    }

    @PostMapping(path = "/ban-player")
    public ResponseEntity<Void> banPlayer(@SessionAttribute AdminEntity userInfo, HttpSession httpSession, HttpServletRequest request, HttpServletResponse response, @RequestParam long playerSteamId, @RequestParam String banLength, @RequestParam Long banLengthInTimeStamp, @RequestParam String banReason) {
        LOGGER.debug("Received secured {} request on '{}' with userInfo in cookie '{}'", request.getMethod(), request.getRequestURL(), userInfo);
        Timestamp expirationTime = new Timestamp(System.currentTimeMillis() + banLengthInTimeStamp);
        banReason += " До " + expirationTime;
        Rcon.command("AdminBan " + playerSteamId + " " + banLength + " " + banReason);
        entityManager.addPlayerBan(playerSteamId, userInfo.getSteamId(), banLength.equalsIgnoreCase("0") ? null : expirationTime, banReason);
        LOGGER.info("Admin '{}' has banned player '{}' by reason '{}' for length '{}'", userInfo.getName(), playerSteamId, banReason, banLength);
        return ResponseEntity.ok().build();
    }

    @PostMapping(path = "/unban-player")
    public ResponseEntity<Void> unbanPlayer(@SessionAttribute AdminEntity userInfo, HttpSession httpSession, HttpServletRequest request, HttpServletResponse response, @RequestParam int banId) {
        LOGGER.debug("Received secured {} request on '{}' with userInfo in cookie '{}'", request.getMethod(), request.getRequestURL(), userInfo);
        entityManager.unbanPlayerBan(banId, userInfo.getSteamId());
        LOGGER.info("Admin '{}' unban '{}' banId", userInfo.getSteamId(), banId);
        return ResponseEntity.ok().build();
    }

    @PostMapping(path = "/get-player-punishment-history")
    public ResponseEntity<HashMap<String, Object>> getPlayerPunismentHistory(@SessionAttribute AdminEntity userInfo, HttpSession httpSession, HttpServletRequest request, HttpServletResponse response, @RequestParam long playerSteamId) {
        LOGGER.debug("Received secured {} request on '{}' with userInfo in cookie '{}'", request.getMethod(), request.getRequestURL(), userInfo);
        PlayerEntity player = entityManager.getPlayerBySteamId(playerSteamId);
        if (player == null) {
            return ResponseEntity.status(404).build();
        }
        return ResponseEntity.ok(new HashMap<>() {{
            put("bans", player.getPlayersBansBySteamId());
            put("kicks", player.getPlayersKicksBySteamId());
        }});
    }

    @PostMapping(path = "/get-player-bans")
    public ResponseEntity<Collection<PlayerBanEntity>> getPlayerBans(@SessionAttribute AdminEntity userInfo, HttpSession httpSession, HttpServletRequest request, HttpServletResponse response, @RequestParam long playerSteamId) {
        LOGGER.debug("Received secured {} request on '{}' with userInfo in cookie '{}'", request.getMethod(), request.getRequestURL(), userInfo);
        PlayerEntity player = entityManager.getPlayerBySteamId(playerSteamId);
        if (player == null) {
            return ResponseEntity.status(404).build();
        }
        return ResponseEntity.ok(player.getPlayersBansBySteamId());
    }

    @PostMapping(path = "/get-bans")
    public ResponseEntity<HashMap<String, Object>> getAdmin(@SessionAttribute AdminEntity userInfo, HttpSession httpSession, HttpServletRequest request, HttpServletResponse response, @RequestParam boolean showOnlyActiveBans, @RequestParam int page, @RequestParam int size) {
        LOGGER.debug("Received secured {} request on '{}' with userInfo in cookie '{}'", request.getMethod(), request.getRequestURL(), userInfo);
        if (size > 100) {
            return ResponseEntity.status(BAD_REQUEST).build();
        }

        Page<PlayerBanEntity> resultPage = showOnlyActiveBans ?
                playerBanService.findAllActiveBans(PageRequest.of(page, size, Sort.by("id").descending()))
                : playerBanService.findAll(PageRequest.of(page, size, Sort.by("id").descending()));
        HashMap<String, Object> map = new HashMap<>() {{
            put("currentPage", resultPage.getNumber());
            put("totalPages", resultPage.getTotalPages());
            put("totalElements", resultPage.getTotalElements());
            put("hasNext", resultPage.hasNext());
            put("nextPage", resultPage.hasNext() ? resultPage.nextPageable().getPageNumber() : null);
            put("hasPrevious", resultPage.hasPrevious());
            put("previousPage", resultPage.hasPrevious() ? resultPage.previousPageable().getPageNumber() : null);
        }};
        List<LinkedHashMap<String, Object>> contentList = new ArrayList<>();

        resultPage.getContent().forEach(ban -> contentList.add(
                new LinkedHashMap<>() {{
                    put("id", ban.getId());
                    put("bannedPlayer", new HashMap<>() {{
                        put("playerName", ban.getPlayersBySteamId().getName());
                        put("steamId", ban.getPlayersBySteamId().getSteamId().toString());
                    }});
                    put("bannedBy", new HashMap<>() {{
                        put("adminName", ban.getAdminsBySteamId().getName());
                        put("steamId", ban.getAdminsBySteamId().getSteamId().toString());
                    }});
                    put("reason", ban.getReason());
                    put("isUnbannedManual", ban.getIsUnbannedManually());
                    put("unbannedManualBy",
                            ban.getIsUnbannedManually() ?
                                    new HashMap<>() {{
                                        put("adminName", ban.getUnbannedAdminBySteamId().getName());
                                        put("steamId", ban.getUnbannedAdminBySteamId().getSteamId().toString());
                                    }}
                                    : null
                    );
                    put("manualUnbannedTime", ban.getUnbannedTime());
                    put("expirationTime", ban.getExpirationTime());
                    put("creationTime", ban.getCreationTime());
                }})
        );
        map.put("content", contentList);
        return ResponseEntity.ok(map);
    }

    @PostMapping(path = "/kick-player")
    public ResponseEntity<Void> kickPlayer(@SessionAttribute AdminEntity userInfo, HttpSession httpSession, HttpServletRequest request, HttpServletResponse response, @RequestParam long playerSteamId, @RequestParam String kickReason) {
        LOGGER.debug("Received secured {} request on '{}' with userInfo in cookie '{}'", request.getMethod(), request.getRequestURL(), userInfo);
        String rconResponse = Rcon.command("AdminKick " + playerSteamId + " " + kickReason);
        if (rconResponse == null || !rconResponse.contains("Kicked player")) {
            return ResponseEntity.status(BAD_REQUEST).build();
        }
        entityManager.addPlayerKick(playerSteamId, userInfo.getSteamId(), kickReason);
        entityManager.addAdminActionInLog(userInfo.getSteamId(), playerSteamId, "KickPlayer", kickReason);
        return ResponseEntity.ok().build();
    }

    @PostMapping(path = "/get-player-kicks")
    public ResponseEntity<Collection<PlayerKickEntity>> getPlayerKicks(@SessionAttribute AdminEntity userInfo, HttpSession httpSession, HttpServletRequest request, HttpServletResponse response, @RequestParam long playerSteamId) {
        LOGGER.debug("Received secured {} request on '{}' with userInfo in cookie '{}'", request.getMethod(), request.getRequestURL(), userInfo);
        PlayerEntity player = entityManager.getPlayerBySteamId(playerSteamId);
        if (player == null) {
            return ResponseEntity.status(404).build();
        }
        return ResponseEntity.ok(player.getPlayersKicksBySteamId());
    }

    @PostMapping(path = "/warn-player")
    public ResponseEntity<Void> warnPlayer(@SessionAttribute AdminEntity userInfo,
                                           HttpSession httpSession,
                                           HttpServletRequest request,
                                           HttpServletResponse response,
                                           @RequestParam long playerSteamId,
                                           @RequestParam String warnReason) {
        LOGGER.debug("Received secured {} request on '{}' with userInfo in cookie '{}'", request.getMethod(), request.getRequestURL(), userInfo);
        String rconResponse = Rcon.command(String.format("AdminWarn %s %s ", playerSteamId, warnReason));
        if (rconResponse == null || !rconResponse.contains("Remote admin has warned player")) {
            return ResponseEntity.status(BAD_REQUEST).build();
        }
        entityManager.addAdminActionInLog(userInfo.getSteamId(), playerSteamId, "WarnPlayer", warnReason);
        return ResponseEntity.ok().build();
    }

    @PostMapping(path = "/warn-squad")
    public ResponseEntity<Void> warnSquad(@SessionAttribute AdminEntity userInfo,
                                          HttpSession httpSession,
                                          HttpServletRequest request,
                                          HttpServletResponse response,
                                          @RequestParam int squadId,
                                          @RequestParam int teamId,
                                          @RequestParam String warnReason) {
        LOGGER.debug("Received secured {} request on '{}' with userInfo in cookie '{}'", request.getMethod(), request.getRequestURL(), userInfo);
        SquadServer.getOnlinePlayers().forEach(onlinePlayer -> {
            if (onlinePlayer.getSquadID() == squadId && onlinePlayer.getTeamId() == teamId) {
                Rcon.command(String.format("AdminWarn %s %s ", onlinePlayer.getSteamId(), warnReason));
            }
        });

        entityManager.addAdminActionInLog(
                userInfo.getSteamId(),
                null,
                "WarnSquad",
                "(Warn to squad " + squadId + " in team " + teamId + ") | " + warnReason
        );
        return ResponseEntity.ok().build();
    }

    @PostMapping(path = "/send-broadcast")
    public ResponseEntity<Void> broadcastMessage(@SessionAttribute AdminEntity userInfo, HttpSession httpSession, HttpServletRequest request, HttpServletResponse response, @RequestParam String broadcastMessage) {
        LOGGER.debug("Received secured {} request on '{}' with userInfo in cookie '{}'", request.getMethod(), request.getRequestURL(), userInfo);
        String rconResponse = Rcon.command(String.format("AdminBroadcast %s ", broadcastMessage));
        if (rconResponse == null || !rconResponse.contains("Message broadcasted")) {
            return ResponseEntity.status(BAD_REQUEST).build();
        }
        entityManager.addAdminActionInLog(userInfo.getSteamId(), null, "SendBroadcast", broadcastMessage);
        return ResponseEntity.ok().build();
    }

    @PostMapping(path = "/player-team-change")
    public ResponseEntity<Void> playerTeamChange(@SessionAttribute AdminEntity userInfo, HttpSession httpSession, HttpServletRequest request, HttpServletResponse response, @RequestParam long playerSteamId) {
        LOGGER.debug("Received secured {} request on '{}' with userInfo in cookie '{}'", request.getMethod(), request.getRequestURL(), userInfo);
        String rconResponse = Rcon.command("AdminForceTeamChange " + playerSteamId);
        if (rconResponse == null || !rconResponse.contains("Forced team change for player")) {
            return ResponseEntity.status(BAD_REQUEST).build();
        }
        entityManager.addAdminActionInLog(userInfo.getSteamId(), playerSteamId, "PlayerTeamChange", null);
        LOGGER.info("Admin '{}' has forced team change for player: '{}'", userInfo.getName(), playerSteamId);
        return ResponseEntity.ok().build();
    }

    @PostMapping(path = "/remove-player-from-squad")
    public ResponseEntity<Void> removePlayerFromSquad(@SessionAttribute AdminEntity userInfo, HttpSession httpSession, HttpServletRequest request, HttpServletResponse response, @RequestParam long playerSteamId) {
        LOGGER.debug("Received secured {} request on '{}' with userInfo in cookie '{}'", request.getMethod(), request.getRequestURL(), userInfo);
        String rconResponse = Rcon.command("AdminRemovePlayerFromSquad " + playerSteamId);
        if (rconResponse == null || !rconResponse.contains("was removed from squad")) {
            return ResponseEntity.status(BAD_REQUEST).build();
        }
        entityManager.addAdminActionInLog(userInfo.getSteamId(), playerSteamId, "RemovePlayerFromSquad", null);
        LOGGER.info("Admin '{}' has removed player '{}' from squad", userInfo.getSteamId(), playerSteamId);
        return ResponseEntity.ok().build();
    }

    @PostMapping(path = "/add-player-note")
    public ResponseEntity<Void> addPlayerNote(@SessionAttribute AdminEntity userInfo, HttpSession httpSession, HttpServletRequest request, HttpServletResponse response, @RequestParam long playerSteamId, @RequestParam String note) {
        LOGGER.debug("Received secured {} request on '{}' with userInfo in cookie '{}'", request.getMethod(), request.getRequestURL(), userInfo);
        entityManager.addPlayerNote(playerSteamId, userInfo.getSteamId(), note);
        return ResponseEntity.ok().build();
    }

    @PostMapping(path = "/delete-player-note")
    public ResponseEntity<Void> deletePlayerNote(@SessionAttribute AdminEntity userInfo, HttpSession httpSession, HttpServletRequest request, HttpServletResponse response, @RequestParam long playerSteamId, @RequestParam int noteId) {
        LOGGER.debug("Received secured {} request on '{}' with userInfo in cookie '{}'", request.getMethod(), request.getRequestURL(), userInfo);
        String noteText = entityManager.getPlayerNote(noteId).getNote();
        entityManager.deletePlayerNote(noteId);
        entityManager.addAdminActionInLog(userInfo.getSteamId(), playerSteamId, "DeletePlayerNote", noteText);
        return ResponseEntity.ok().build();
    }

    @PostMapping(path = "/get-player-notes")
    public ResponseEntity<Collection<PlayerNoteEntity>> getPlayerNotes(@SessionAttribute AdminEntity userInfo, HttpSession httpSession, HttpServletRequest request, HttpServletResponse response, @RequestParam long playerSteamId) {
        LOGGER.debug("Received secured {} request on '{}' with userInfo in cookie '{}'", request.getMethod(), request.getRequestURL(), userInfo);
        PlayerEntity player = entityManager.getPlayerBySteamId(playerSteamId);
        if (player == null) {
            return ResponseEntity.status(404).build();
        }
        return ResponseEntity.ok(player.getPlayersNotesBySteamId());
    }

    /*TODO*/
    @PostMapping(path = "/get-player-messages")
    public ResponseEntity<Collection<PlayerMessageEntity>> getPlayerMessages(@SessionAttribute AdminEntity userInfo, HttpSession httpSession, HttpServletRequest request, HttpServletResponse response, @RequestParam long playerSteamId) {
        LOGGER.debug("Received secured {} request on '{}' with userInfo in cookie '{}'", request.getMethod(), request.getRequestURL(), userInfo);
        PlayerEntity player = entityManager.getPlayerBySteamId(playerSteamId);
        if (player == null) {
            return ResponseEntity.status(404).build();
        }
        return ResponseEntity.ok(player.getPlayersMessagesBySteamId());
    }

    @PostMapping(path = "/get-admins")
    public ResponseEntity<List<HashMap<String, Object>>> getAdmins(@SessionAttribute AdminEntity userInfo, HttpSession httpSession, HttpServletRequest request, HttpServletResponse response) {
        LOGGER.debug("Received secured {} request on '{}' with userInfo in cookie '{}'", request.getMethod(), request.getRequestURL(), userInfo);
        List<HashMap<String, Object>> list = new ArrayList<>();
        adminService.findAll().forEach(admin -> list.add(new HashMap<>() {{
            put("steamId", admin.getSteamId());
            put("name", admin.getName());
            put("steamSign", admin.getSteamSign());
            put("role", admin.getRole());
            put("avatar", admin.getAvatar());
            put("avatarMedium", admin.getAvatarMedium());
            put("avatarFull", admin.getAvatarFull());
            put("createTime", admin.getCreateTime());
            put("modifiedTime", admin.getModifiedTime());
        }}));
        return ResponseEntity.ok(list);
    }

    @PostMapping(path = "/get-admin-actions")
    public ResponseEntity<HashMap<String, Object>> getAdmin(@SessionAttribute AdminEntity userInfo, HttpSession httpSession, HttpServletRequest request, HttpServletResponse response, @RequestParam long adminSteamId, @RequestParam int page, @RequestParam int size) {
        LOGGER.debug("Received secured {} request on '{}' with userInfo in cookie '{}'", request.getMethod(), request.getRequestURL(), userInfo);
        if (size > 100) {
            return ResponseEntity.status(BAD_REQUEST).build();
        }
        Page<AdminActionLogEntity> resultPage = adminActionLogsService.findAllBy(adminSteamId, PageRequest.of(page, size, Sort.by("id").descending()));
        HashMap<String, Object> map = new HashMap<>() {{
            put("currentPage", resultPage.getNumber());
            put("totalPages", resultPage.getTotalPages());
            put("totalElements", resultPage.getTotalElements());
            put("hasNext", resultPage.hasNext());
            put("nextPage", resultPage.hasNext() ? resultPage.nextPageable().getPageNumber() : null);
            put("hasPrevious", resultPage.hasPrevious());
            put("previousPage", resultPage.hasPrevious() ? resultPage.previousPageable().getPageNumber() : null);
        }};
        List<HashMap<String, Object>> contentList = new ArrayList<>();

        resultPage.getContent().forEach(adminActionLogEntity -> {
            HashMap<String, Object> playerByAdminId = null;
            PlayerEntity player = adminActionLogEntity.getPlayerByAdminId();
            if (player != null) {
                playerByAdminId = new HashMap<>() {{
                    put("steamId", player.getSteamId());
                    put("name", player.getName());
                    put("createTime", player.getCreateTime());
                    put("playersBansBySteamId", player.getPlayersBansBySteamId().size());
                    put("playersMessagesBySteamId", player.getPlayersMessagesBySteamId().size());
                    put("playersNotesBySteamId", player.getPlayersNotesBySteamId().size());
                    put("playersKicksBySteamId", player.getPlayersKicksBySteamId().size());
                }};
            }
            HashMap<String, Object> finalPlayerByAdminId = playerByAdminId;
            HashMap<String, Object> contentMap = new HashMap<>() {{
                put("id", adminActionLogEntity.getId());
                put("action", adminActionLogEntity.getAction());
                put("reason", adminActionLogEntity.getReason());
                put("createTime", adminActionLogEntity.getCreateTime());
                put("playerByAdminId", finalPlayerByAdminId);
            }};
            contentList.add(contentMap);
        });
        map.put("content", contentList);
        return ResponseEntity.ok(map);
    }

    @PostMapping(path = "/get-admins-actions")
    public ResponseEntity<HashMap<String, Object>> getAdminActions(@SessionAttribute AdminEntity userInfo, HttpSession httpSession, HttpServletRequest request, HttpServletResponse response, @RequestParam int page, @RequestParam int size) {
        LOGGER.debug("Received secured {} request on '{}' with userInfo in cookie '{}'", request.getMethod(), request.getRequestURL(), userInfo);
        if (size > 100) {
            return ResponseEntity.status(BAD_REQUEST).build();
        }
        Page<AdminActionLogEntity> resultPage = adminActionLogsService.findAll(PageRequest.of(page, size, Sort.by("id").descending()));
        HashMap<String, Object> map = new HashMap<>() {{
            put("currentPage", resultPage.getNumber());
            put("totalPages", resultPage.getTotalPages());
            put("totalElements", resultPage.getTotalElements());
            put("hasNext", resultPage.hasNext());
            put("nextPage", resultPage.hasNext() ? resultPage.nextPageable().getPageNumber() : null);
            put("hasPrevious", resultPage.hasPrevious());
            put("previousPage", resultPage.hasPrevious() ? resultPage.previousPageable().getPageNumber() : null);
        }};
        List<HashMap<String, Object>> contentList = new ArrayList<>();

        resultPage.getContent().forEach(adminActionLogEntity -> {
            HashMap<String, Object> playerByAdminId = null;
            PlayerEntity player = adminActionLogEntity.getPlayerByAdminId();
            if (player != null) {
                playerByAdminId = new HashMap<>() {{
                    put("steamId", player.getSteamId());
                    put("name", player.getName());
                    put("createTime", player.getCreateTime());
                    put("playersBansBySteamId", player.getPlayersBansBySteamId().size());
                    put("playersMessagesBySteamId", player.getPlayersMessagesBySteamId().size());
                    put("playersNotesBySteamId", player.getPlayersNotesBySteamId().size());
                    put("playersKicksBySteamId", player.getPlayersKicksBySteamId().size());
                }};
            }
            HashMap<String, Object> finalPlayerByAdminId = playerByAdminId;
            HashMap<String, Object> contentMap = new HashMap<>() {{
                put("id", adminActionLogEntity.getId());
                put("action", adminActionLogEntity.getAction());
                put("reason", adminActionLogEntity.getReason());
                put("createTime", adminActionLogEntity.getCreateTime());
                put("adminsByAdminId", adminActionLogEntity.getAdminsByAdminId().getName());
                put("playerByAdminId", finalPlayerByAdminId);
            }};
            contentList.add(contentMap);
        });
        map.put("content", contentList);
        return ResponseEntity.ok(map);
    }

    /*TODO*/
    @PostMapping(path = "/get-admin-actions-by-text")
    public ResponseEntity<Collection<AdminActionLogEntity>> getAdminActionsByContainsText(@SessionAttribute AdminEntity userInfo, HttpSession httpSession, HttpServletRequest request, HttpServletResponse response, @RequestParam String logText) {
        LOGGER.debug("Received secured {} request on '{}' with userInfo in cookie '{}'", request.getMethod(), request.getRequestURL(), userInfo);
        return ResponseEntity.ok().build();
    }

    @PostMapping(path = "/disband-squad")
    public ResponseEntity<Void> disbandSquad(@SessionAttribute AdminEntity userInfo, HttpSession httpSession, HttpServletRequest request, HttpServletResponse response, @RequestParam String teamId, @RequestParam String squadId, @RequestParam String squadName) {
        LOGGER.debug("Received secured {} request on '{}' with userInfo in cookie '{}'", request.getMethod(), request.getRequestURL(), userInfo);
        String rconResponse = Rcon.command("AdminDisbandSquad " + teamId + " " + squadId);
        if (rconResponse == null || !rconResponse.contains("Remote admin disbanded squad")) {
            return ResponseEntity.status(BAD_REQUEST).build();
        }
        entityManager.addAdminActionInLog(userInfo.getSteamId(), null, "DisbandSquad", "Расформировал отряд " + squadName + " (" + squadId + ") в команде " + teamId);
        LOGGER.info("Admin '{}' has disbanded squad '{}' in team '{}'", userInfo.getName(), squadId, teamId);
        return ResponseEntity.ok().build();
    }

    @PostMapping(path = "/change-current-layer")
    public ResponseEntity<Collection<AdminActionLogEntity>> changeCurrentLayer(@SessionAttribute AdminEntity userInfo, HttpSession httpSession, HttpServletRequest request, HttpServletResponse response, @RequestParam String layerName) {
        LOGGER.debug("Received secured {} request on '{}' with userInfo in cookie '{}'", request.getMethod(), request.getRequestURL(), userInfo);
        Rcon.command("AdminChangeLayer " + layerName);
        entityManager.addAdminActionInLog(userInfo.getSteamId(), null, "ChangeCurrentLayer", layerName);
        LOGGER.info("Admin '{}' has changed current layer to '{}'", userInfo.getName(), layerName);
        return ResponseEntity.ok().build();
    }

    @PostMapping(path = "/change-next-layer")
    public ResponseEntity<Collection<AdminActionLogEntity>> changeNextLayer(@SessionAttribute AdminEntity userInfo, HttpSession httpSession, HttpServletRequest request, HttpServletResponse response, @RequestParam String layerName) {
        LOGGER.debug("Received secured {} request on '{}' with userInfo in cookie '{}'", request.getMethod(), request.getRequestURL(), userInfo);
        Rcon.command("AdminSetNextLayer " + layerName);
        entityManager.addAdminActionInLog(userInfo.getSteamId(), null, "ChangeNextLayer", layerName);
        LOGGER.info("Admin '{}' has changed next layer to '{}'", userInfo.getName(), layerName);
        RconUpdater.updateLayerInfo();
        return ResponseEntity.ok().build();
    }

    @GetMapping(path = "/auth")
    public ResponseEntity<Void> auth(@SessionAttribute AdminEntity userInfo, HttpSession httpSession, HttpServletRequest request, HttpServletResponse response) {
        LOGGER.debug("Received secured GET request on '{}' with userInfo in cookie '{}'", request.getRequestURL(), userInfo);
        return entityManager.getAdminBySteamID(userInfo.getSteamId()) == null ? ResponseEntity.ok(null) : ResponseEntity.status(401).build();
    }

    @GetMapping(path = "/user-logout")
    public ResponseEntity<Void> userLogout(@SessionAttribute AdminEntity userInfo, HttpSession httpSession, HttpServletRequest request, HttpServletResponse response) {
        LOGGER.debug("Received secured GET request on '{}' with userInfo in cookie '{}'", request.getRequestURL(), userInfo);
        httpSession.removeAttribute("userInfo");
        httpSession.invalidate();
        return httpSession.getAttribute("userInfo") == null ? ResponseEntity.ok().build() : ResponseEntity.status(500).build();
    }

}
