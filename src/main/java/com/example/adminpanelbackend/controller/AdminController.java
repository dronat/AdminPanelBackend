package com.example.adminpanelbackend.controller;

import com.example.adminpanelbackend.dataBase.entity.AdminActionLogEntity;
import com.example.adminpanelbackend.dataBase.entity.AdminEntity;
import com.example.adminpanelbackend.dataBase.entity.PlayerEntity;
import com.woop.Squad4J.a2s.Query;
import com.woop.Squad4J.model.OnlineInfo;
import com.woop.Squad4J.server.RconUpdater;
import com.woop.Squad4J.server.tailer.FtpBanService;
import com.woop.Squad4J.server.tailer.FtpLogTailer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
public class AdminController extends BaseSecureController {
    private static final Logger LOGGER = LoggerFactory.getLogger(AdminController.class);

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
    public ResponseEntity<HashMap<String, Object>> getAdminActions(@SessionAttribute AdminEntity userInfo, HttpSession httpSession, HttpServletRequest request, HttpServletResponse response, @RequestParam long adminSteamId, @RequestParam int page, @RequestParam int size) {
        LOGGER.debug("Received secured {} request on '{}' with userInfo in cookie '{}'", request.getMethod(), request.getRequestURL(), userInfo);
        if (size > 100) {
            return ResponseEntity.status(BAD_REQUEST).build();
        }
        Page<AdminActionLogEntity> resultPage = adminActionLogsService.findAllByAdmin(adminSteamId, PageRequest.of(page, size, Sort.by("id").descending()));
        HashMap<String, Object> map = getMapForPagination(resultPage);

        List<HashMap<String, Object>> contentList = new ArrayList<>();

        resultPage.getContent().forEach(adminActionLogEntity -> {
            HashMap<String, Object> playerByAdminId = null;
            PlayerEntity player = adminActionLogEntity.getPlayer();
            if (player != null) {
                playerByAdminId = new HashMap<>() {{
                    put("steamId", player.getSteamId());
                    put("name", player.getName());
                    put("createTime", player.getCreateTime());
                    put("playersBansBySteamId", player.getPlayerBans().size());
                    put("playersMessagesBySteamId", player.getPlayerMessages().size());
                    put("playersNotesBySteamId", player.getPlayerNotes().size());
                    put("playersKicksBySteamId", player.getPlayerKicks().size());
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

    @PostMapping(path = "/get-admin-actions-with-params")
    public ResponseEntity<HashMap<String, Object>> getAdminActionsWithParams(
            @SessionAttribute AdminEntity userInfo,
            HttpSession httpSession,
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestParam String adminSteamId,
            @RequestParam String playerSteamId,
            @RequestParam List<String> actions,
            @RequestParam long dateFrom,
            @RequestParam long dateTo,
            @RequestParam int page,
            @RequestParam int size) {
        LOGGER.debug("Received secured {} request on '{}' with userInfo in cookie '{}'", request.getMethod(), request.getRequestURL(), userInfo);
        if (size > 100) {
            return ResponseEntity.status(BAD_REQUEST).build();
        }
        Page<AdminActionLogEntity> resultPage = adminActionLogsService.findAllByParams(adminSteamId, playerSteamId, actions, new Timestamp(dateFrom), new Timestamp(dateTo), PageRequest.of(page, size, Sort.by("id").descending()));
        HashMap<String, Object> map = getMapForPagination(resultPage);

        List<HashMap<String, Object>> contentList = new ArrayList<>();

        resultPage.getContent().forEach(adminActionLogEntity -> {
            HashMap<String, Object> playerByAdminId = null;
            PlayerEntity player = adminActionLogEntity.getPlayer();
            AdminEntity admin = adminActionLogEntity.getAdmin();
            if (player != null) {
                playerByAdminId = new HashMap<>() {{
                    put("steamId", player.getSteamId());
                    put("name", player.getName());
                    put("createTime", player.getCreateTime());
                    put("playersBansBySteamId", player.getPlayerBans().size());
                    put("playersMessagesBySteamId", player.getPlayerMessages().size());
                    put("playersNotesBySteamId", player.getPlayerNotes().size());
                    put("playersKicksBySteamId", player.getPlayerKicks().size());
                }};
            }
            HashMap<String, Object> finalPlayerByAdminId = playerByAdminId;
            HashMap<String, Object> contentMap = new HashMap<>() {{
                put("id", adminActionLogEntity.getId());
                put("action", adminActionLogEntity.getAction());
                put("reason", adminActionLogEntity.getReason());
                put("adminName", admin.getName());
                put("adminSteamId", admin.getSteamId());
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
        HashMap<String, Object> map = getMapForPagination(resultPage);

        List<HashMap<String, Object>> contentList = new ArrayList<>();

        resultPage.getContent().forEach(adminActionLogEntity -> {
            HashMap<String, Object> playerByAdminId = null;
            PlayerEntity player = adminActionLogEntity.getPlayer();
            if (player != null) {
                playerByAdminId = new HashMap<>() {{
                    put("steamId", player.getSteamId());
                    put("name", player.getName());
                    put("createTime", player.getCreateTime());
                    put("playersBansBySteamId", player.getPlayerBans().size());
                    put("playersMessagesBySteamId", player.getPlayerMessages().size());
                    put("playersNotesBySteamId", player.getPlayerNotes().size());
                    put("playersKicksBySteamId", player.getPlayerKicks().size());
                }};
            }
            HashMap<String, Object> finalPlayerByAdminId = playerByAdminId;
            HashMap<String, Object> contentMap = new HashMap<>() {{
                put("id", adminActionLogEntity.getId());
                put("action", adminActionLogEntity.getAction());
                put("reason", adminActionLogEntity.getReason());
                put("createTime", adminActionLogEntity.getCreateTime());
                put("adminsByAdminId", adminActionLogEntity.getAdmin().getName());
                put("playerByAdminId", finalPlayerByAdminId);
            }};
            contentList.add(contentMap);
        });
        map.put("content", contentList);
        return ResponseEntity.ok(map);
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
        httpSession.removeAttribute(FindByIndexNameSessionRepository.PRINCIPAL_NAME_INDEX_NAME);
        httpSession.invalidate();
        return ResponseEntity.ok().build();
    }

    @GetMapping(path = "/get-backend-status")
    public ResponseEntity<HashMap<String, Object>> getBackendStatus(@SessionAttribute AdminEntity userInfo, HttpSession httpSession, HttpServletRequest request, HttpServletResponse response) {
        LOGGER.debug("Received secured GET request on '{}' with userInfo in cookie '{}'", request.getRequestURL(), userInfo);
        return ResponseEntity.ok(new HashMap<>() {{
            put("RconUpdater", RconUpdater.lastSuccessfullyWork);
            put("QueryUpdater", Query.lastSuccessfullyWork);
            put("FtpLogTailer", FtpLogTailer.lastSuccessfullyWork);
            put("FtpBanService", FtpBanService.lastSuccessfullyWork);
        }});
    }
}
