package com.example.adminpanelbackend.controller;

import com.example.adminpanelbackend.Role;
import com.example.adminpanelbackend.db.entity.AdminActionLogEntity;
import com.example.adminpanelbackend.db.entity.AdminEntity;
import com.example.adminpanelbackend.db.entity.LayerHistoryEntity;
import com.example.adminpanelbackend.db.entity.ServerEntity;
import com.woop.Squad4J.dto.rcon.DisconnectedPlayer;
import com.woop.Squad4J.dto.rcon.OnlineInfo;
import com.woop.Squad4J.event.rcon.ChatMessageEvent;
import com.woop.Squad4J.rcon.Rcon;
import com.woop.Squad4J.server.RconUpdater;
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import static com.example.adminpanelbackend.RoleEnum.BASE;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@RestController()
@EnableJdbcHttpSession(maxInactiveIntervalInSeconds = 604800)
@CrossOrigin
public class SquadServerController extends BaseSecureController {
    private static final Logger LOGGER = LoggerFactory.getLogger(SquadServerController.class);

    @Role(role = BASE)
    @GetMapping(path = "/get-online-players")
    public ResponseEntity<OnlineInfo> getOnline(@SessionAttribute AdminEntity userInfo, HttpSession httpSession, HttpServletRequest request, HttpServletResponse response) {
        LOGGER.debug("Received secured {} request on '{}' with userInfo in cookie '{}'", request.getMethod(), request.getRequestURL(), userInfo);
        return ResponseEntity.ok(SquadServer.getOnlineTeamsWithSquadsAndPlayers());
    }


    @Role(role = BASE)
    @GetMapping(path = "/get-chat-messages")
    public ResponseEntity<Collection<ChatMessageEvent>> getChatMessages(@SessionAttribute AdminEntity userInfo, HttpSession httpSession, HttpServletRequest request, HttpServletResponse response) {
        LOGGER.debug("Received secured {} request on '{}' with userInfo in cookie '{}'", request.getMethod(), request.getRequestURL(), userInfo);
        return ResponseEntity.ok(SquadServer.getChatMessages());
    }


    @Role(role = BASE)
    @GetMapping(path = "/get-disconnected-players")
    public ResponseEntity<Collection<DisconnectedPlayer>> getDisconnectedPlayers(@SessionAttribute AdminEntity userInfo, HttpSession httpSession, HttpServletRequest request, HttpServletResponse response) {
        LOGGER.debug("Received secured {} request on '{}' with userInfo in cookie '{}'", request.getMethod(), request.getRequestURL(), userInfo);
        return ResponseEntity.ok(SquadServer.getDisconnectedPlayers());
    }

    @Role(role = BASE)
    @GetMapping(path = "/get-server-info")
    public ResponseEntity<HashMap<String, Object>> getServerInfo(@SessionAttribute AdminEntity userInfo, HttpSession httpSession, HttpServletRequest request, HttpServletResponse response) {
        LOGGER.debug("Received secured {} request on '{}' with userInfo in cookie '{}'", request.getMethod(), request.getRequestURL(), userInfo);
        return ResponseEntity.ok(SquadServer.getServerInfo());
    }

    @Role(role = BASE)
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

    @Role(role = BASE)
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

    @Role(role = BASE)
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

    @Role(role = BASE)
    @PostMapping(path = "/get-layers-history")
    public ResponseEntity<HashMap<String, Object>> getLayersHistory(@SessionAttribute AdminEntity userInfo, HttpSession httpSession, HttpServletRequest request, HttpServletResponse response, @RequestParam int page, @RequestParam int size) {
        LOGGER.debug("Received secured {} request on '{}' with userInfo in cookie '{}'", request.getMethod(), request.getRequestURL(), userInfo);
        if (size > 100) {
            return ResponseEntity.status(BAD_REQUEST).build();
        }
        ServerEntity server = serversService.findById(SERVER_ID).orElseThrow();
        Page<LayerHistoryEntity> resultPage = layersHistoryService.findAllByServerId(server ,PageRequest.of(page, size, Sort.by("id").descending()));
        HashMap<String, Object> map = getMapForPagination(resultPage);

        List<HashMap<String, Object>> contentList = new ArrayList<>();

        resultPage.getContent().forEach(layerHistoryEntity -> {
            HashMap<String, Object> contentMap = new HashMap<>() {{
                put("id", layerHistoryEntity.getId());
                put("layer", layerHistoryEntity.getLayer());
                put("time", layerHistoryEntity.getCreationTime());
            }};
            contentList.add(contentMap);
        });
        map.put("server", server);
        map.put("content", contentList);
        return ResponseEntity.ok(map);
    }

    @Role(role = BASE)
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

    @Role(role = BASE)
    @PostMapping(path = "/change-current-layer")
    public ResponseEntity<Collection<AdminActionLogEntity>> changeCurrentLayer(@SessionAttribute AdminEntity userInfo, HttpSession httpSession, HttpServletRequest request, HttpServletResponse response, @RequestParam String layerName) {
        LOGGER.debug("Received secured {} request on '{}' with userInfo in cookie '{}'", request.getMethod(), request.getRequestURL(), userInfo);
        Rcon.command("AdminChangeLayer " + layerName);
        entityManager.addAdminActionInLog(userInfo.getSteamId(), null, "ChangeCurrentLayer", layerName);
        LOGGER.info("Admin '{}' has changed current layer to '{}'", userInfo.getName(), layerName);
        return ResponseEntity.ok().build();
    }

    @Role(role = BASE)
    @PostMapping(path = "/change-next-layer")
    public ResponseEntity<Collection<AdminActionLogEntity>> changeNextLayer(@SessionAttribute AdminEntity userInfo, HttpSession httpSession, HttpServletRequest request, HttpServletResponse response, @RequestParam String layerName) {
        LOGGER.debug("Received secured {} request on '{}' with userInfo in cookie '{}'", request.getMethod(), request.getRequestURL(), userInfo);
        Rcon.command("AdminSetNextLayer " + layerName);
        entityManager.addAdminActionInLog(userInfo.getSteamId(), null, "ChangeNextLayer", layerName);
        LOGGER.info("Admin '{}' has changed next layer to '{}'", userInfo.getName(), layerName);
        RconUpdater.updateLayerInfo();
        return ResponseEntity.ok().build();
    }
}
