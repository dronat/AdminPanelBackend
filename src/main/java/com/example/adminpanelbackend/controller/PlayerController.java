package com.example.adminpanelbackend.controller;

import com.example.adminpanelbackend.SteamService;
import com.example.adminpanelbackend.dataBase.entity.*;
import com.example.adminpanelbackend.model.SteamUserModel;
import com.woop.Squad4J.event.rcon.ChatMessageEvent;
import com.woop.Squad4J.model.DisconnectedPlayer;
import com.woop.Squad4J.model.OnlineInfo;
import com.woop.Squad4J.model.OnlinePlayer;
import com.woop.Squad4J.rcon.Rcon;
import com.woop.Squad4J.server.SquadServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
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
public class PlayerController extends BaseSecureController {
    private static final Logger LOGGER = LoggerFactory.getLogger(PlayerController.class);

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
            if (onlinePlayer.getSquadID() != null
                    && onlinePlayer.getTeamId() != null
                    && onlinePlayer.getSquadID() == squadId
                    && onlinePlayer.getTeamId() == teamId) {
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

    @PostMapping(path = "/get-players")
    public ResponseEntity<HashMap<String, Object>> getPlayers(@SessionAttribute AdminEntity userInfo, HttpSession httpSession, HttpServletRequest request, HttpServletResponse response, @RequestParam int page, @RequestParam int size) {
        LOGGER.debug("Received secured {} request on '{}' with userInfo in cookie '{}'", request.getMethod(), request.getRequestURL(), userInfo);
        if (size > 100) {
            return ResponseEntity.status(BAD_REQUEST).build();
        }
        Page<PlayerEntity> resultPage = playerEntityService.findAll(PageRequest.of(page, size));
        HashMap<String, Object> map = getMapForPagination(resultPage);

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
        PlayerEntity player = playerEntityService.findPlayerEntityBySteamId(steamId);
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
    public ResponseEntity<List<PlayerEntity>> getPlayersByContainsText(@SessionAttribute AdminEntity userInfo, HttpSession httpSession, HttpServletRequest request, HttpServletResponse response, @RequestParam int maxSize, @RequestParam String text) {
        LOGGER.debug("Received secured {} request on '{}' with userInfo in cookie '{}'", request.getMethod(), request.getRequestURL(), userInfo);
        if (maxSize > 10) {
            return ResponseEntity.status(BAD_REQUEST).build();
        }
        return ResponseEntity.ok(playerEntityService.findAllByContainsInNameAndSteamId(text, PageRequest.of(0, maxSize)).getContent());
    }

    @PostMapping(path = "/get-player-punishment-history")
    public ResponseEntity<HashMap<String, Object>> getPlayerPunismentHistory(@SessionAttribute AdminEntity userInfo, HttpSession httpSession, HttpServletRequest request, HttpServletResponse response, @RequestParam long playerSteamId) {
        LOGGER.debug("Received secured {} request on '{}' with userInfo in cookie '{}'", request.getMethod(), request.getRequestURL(), userInfo);
        PlayerEntity player = playerEntityService.findPlayerEntityBySteamId(playerSteamId);
        if (player == null) {
            return ResponseEntity.status(404).build();
        }
        return ResponseEntity.ok(new HashMap<>() {{
            put("bans", player.getPlayersBansBySteamId());
            put("kicks", player.getPlayersKicksBySteamId());
        }});
    }

    @PostMapping(path = "/get-player-bans")
    public ResponseEntity<HashMap<String, Object>> getPlayerBans(@SessionAttribute AdminEntity userInfo, HttpSession httpSession, HttpServletRequest request, HttpServletResponse response, @RequestParam long playerSteamId, @RequestParam int page, @RequestParam int size) {
        LOGGER.debug("Received secured {} request on '{}' with userInfo in cookie '{}'", request.getMethod(), request.getRequestURL(), userInfo);
        if (size > 100) {
            return ResponseEntity.status(BAD_REQUEST).build();
        }

        Page<PlayerBanEntity> resultPage = playerBanService.findAllByPlayersBySteamId(playerSteamId, PageRequest.of(page, size, Sort.by("id").descending()));
        HashMap<String, Object> map = getMapForPagination(resultPage);

        List<LinkedHashMap<String, Object>> contentList = new ArrayList<>();

        resultPage.getContent().forEach(ban -> {
                    AdminEntity admin = ban.getAdminsBySteamId();
                    contentList.add(
                            new LinkedHashMap<>() {{
                                put("id", ban.getId());
                                put("bannedBy", new HashMap<>() {{
                                    put("adminName", admin.getName());
                                    put("steamId", admin.getSteamId().toString());
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
                            }});
                }
        );
        map.put("content", contentList);
        return ResponseEntity.ok(map);
    }

    @PostMapping(path = "/get-bans")
    public ResponseEntity<HashMap<String, Object>> getBans(@SessionAttribute AdminEntity userInfo, HttpSession httpSession, HttpServletRequest request, HttpServletResponse response, @RequestParam boolean showOnlyActiveBans, @RequestParam int page, @RequestParam int size) {
        LOGGER.debug("Received secured {} request on '{}' with userInfo in cookie '{}'", request.getMethod(), request.getRequestURL(), userInfo);
        if (size > 100) {
            return ResponseEntity.status(BAD_REQUEST).build();
        }

        Page<PlayerBanEntity> resultPage = showOnlyActiveBans ?
                playerBanService.findAllActiveBans(PageRequest.of(page, size, Sort.by("id").descending()))
                : playerBanService.findAll(PageRequest.of(page, size, Sort.by("id").descending()));
        HashMap<String, Object> map = getMapForPagination(resultPage);

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

    @PostMapping(path = "/get-player-kicks")
    public ResponseEntity<HashMap<String, Object>> getPlayerKicks(@SessionAttribute AdminEntity userInfo, HttpSession httpSession, HttpServletRequest request, HttpServletResponse response, @RequestParam long playerSteamId, @RequestParam int page, @RequestParam int size) {
        LOGGER.debug("Received secured {} request on '{}' with userInfo in cookie '{}'", request.getMethod(), request.getRequestURL(), userInfo);
        if (size > 100) {
            return ResponseEntity.status(BAD_REQUEST).build();
        }

        Page<PlayerKickEntity> resultPage = playerKickService.findAllByPlayersBySteamId(playerSteamId, PageRequest.of(page, size, Sort.by("id").descending()));
        HashMap<String, Object> map = getMapForPagination(resultPage);

        List<LinkedHashMap<String, Object>> contentList = new ArrayList<>();

        resultPage.getContent().forEach(kick -> {
                    AdminEntity admin = kick.getAdminsBySteamId();
                    contentList.add(
                            new LinkedHashMap<>() {{
                                put("id", kick.getId());
                                put("kickedBy", new HashMap<>() {{
                                    put("adminName", admin.getName());
                                    put("steamId", admin.getSteamId().toString());
                                }});
                                put("reason", kick.getReason());
                                put("creationTime", kick.getCreationTime());
                            }});
                }
        );
        map.put("content", contentList);
        return ResponseEntity.ok(map);
    }

    @PostMapping(path = "/get-player-notes")
    public ResponseEntity<HashMap<String, Object>> getPlayerNotes(@SessionAttribute AdminEntity userInfo, HttpSession httpSession, HttpServletRequest request, HttpServletResponse response, @RequestParam long playerSteamId, @RequestParam int page, @RequestParam int size) {
        LOGGER.debug("Received secured {} request on '{}' with userInfo in cookie '{}'", request.getMethod(), request.getRequestURL(), userInfo);
        if (size > 100) {
            return ResponseEntity.status(BAD_REQUEST).build();
        }

        Page<PlayerNoteEntity> resultPage = playerNoteService.findAllByPlayersBySteamId(playerSteamId, PageRequest.of(page, size, Sort.by("id").descending()));
        HashMap<String, Object> map = getMapForPagination(resultPage);

        List<HashMap<String, Object>> contentList = new ArrayList<>();

        resultPage.getContent().forEach(playerMessageEntity -> {
            AdminEntity admin = playerMessageEntity.getAdminsBySteamId();
            HashMap<String, Object> contentMap = new HashMap<>() {{
                put("id", playerMessageEntity.getId());
                put("adminName", admin.getName());
                put("adminSteamId", admin.getSteamId());
                put("note", playerMessageEntity.getNote());
                put("createTime", playerMessageEntity.getCreationTime());
            }};
            contentList.add(contentMap);
        });
        map.put("content", contentList);
        return ResponseEntity.ok(map);
    }

    @PostMapping(path = "/get-player-messages")
    public ResponseEntity<HashMap<String, Object>> getPlayerMessages(@SessionAttribute AdminEntity userInfo, HttpSession httpSession, HttpServletRequest request, HttpServletResponse response, @RequestParam long playerSteamId, @RequestParam int page, @RequestParam int size) {
        LOGGER.debug("Received secured {} request on '{}' with userInfo in cookie '{}'", request.getMethod(), request.getRequestURL(), userInfo);
        if (size > 100) {
            return ResponseEntity.status(BAD_REQUEST).build();
        }

        Page<PlayerMessageEntity> resultPage = playerMessageService.findAllByPlayersBySteamId(playerSteamId, PageRequest.of(page, size, Sort.by("id").descending()));
        HashMap<String, Object> map = getMapForPagination(resultPage);

        List<HashMap<String, Object>> contentList = new ArrayList<>();

        resultPage.getContent().forEach(playerMessageEntity -> contentList.add(
                new HashMap<>() {{
                    put("id", playerMessageEntity.getId());
                    put("chatType", playerMessageEntity.getChatType());
                    put("message", playerMessageEntity.getMessage());
                    put("createTime", playerMessageEntity.getCreationTime());
                }})
        );
        map.put("content", contentList);
        return ResponseEntity.ok(map);
    }

    @PostMapping(path = "/get-messages-by-contains-text")
    public ResponseEntity<HashMap<String, Object>> getMessagesByContainsText(@SessionAttribute AdminEntity userInfo, HttpSession httpSession, HttpServletRequest request, HttpServletResponse response, @RequestParam String text, @RequestParam int page, @RequestParam int size) {
        LOGGER.debug("Received secured {} request on '{}' with userInfo in cookie '{}'", request.getMethod(), request.getRequestURL(), userInfo);
        if (size > 100) {
            return ResponseEntity.status(BAD_REQUEST).build();
        }

        Page<PlayerMessageEntity> resultPage = playerMessageService.findAllByContainsInNameAndSteamId(text, PageRequest.of(page, size, Sort.by("id").descending()));
        HashMap<String, Object> map = getMapForPagination(resultPage);

        List<HashMap<String, Object>> contentList = new ArrayList<>();

        resultPage.getContent().forEach(playerMessageEntity -> {
            PlayerEntity player = playerMessageEntity.getPlayersBySteamId();
            HashMap<String, Object> contentMap = new HashMap<>() {{
                put("id", playerMessageEntity.getId());
                put("playerName", player.getName());
                put("playerSteamId", player.getSteamId());
                put("chatType", playerMessageEntity.getChatType());
                put("message", playerMessageEntity.getMessage());
                put("createTime", playerMessageEntity.getCreationTime());
            }};
            contentList.add(contentMap);
        });
        map.put("content", contentList);
        return ResponseEntity.ok(map);
    }

    /*@PostMapping(path = "/get-messages")
    public ResponseEntity<HashMap<String, Object>> getMessages(@SessionAttribute AdminEntity userInfo, HttpSession httpSession, HttpServletRequest request, HttpServletResponse response, @RequestParam int page, @RequestParam int size) {
        LOGGER.debug("Received secured {} request on '{}' with userInfo in cookie '{}'", request.getMethod(), request.getRequestURL(), userInfo);
        if (size > 100) {
            return ResponseEntity.status(BAD_REQUEST).build();
        }
        Page<PlayerMessageEntity> resultPage = playerMessageService.findAll(PageRequest.of(page, size, Sort.by("id").descending()));
        HashMap<String, Object> map = getMapForPagination(resultPage);

        List<HashMap<String, Object>> contentList = new ArrayList<>();

        resultPage.getContent().forEach(playerMessageEntity -> {
            PlayerEntity player = playerMessageEntity.getPlayersBySteamId();
            HashMap<String, Object> contentMap = new HashMap<>() {{
                put("id", playerMessageEntity.getId());
                put("playerName", player.getName());
                put("playerSteamId", player.getSteamId());
                put("chatType", playerMessageEntity.getChatType());
                put("message", playerMessageEntity.getMessage());
                put("createTime", playerMessageEntity.getCreationTime());
            }};
            contentList.add(contentMap);
        });
        map.put("content", contentList);
        return ResponseEntity.ok(map);
    }*/
}
