package com.example.adminpanelbackend.controller;

import com.example.adminpanelbackend.db.EntityManager;
import com.example.adminpanelbackend.db.entity.PlayerBanEntity;
import com.example.adminpanelbackend.db.service.*;
import com.woop.Squad4J.util.ConfigLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;
import org.springframework.session.jdbc.config.annotation.web.http.EnableJdbcHttpSession;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

@RestController()
@EnableJdbcHttpSession(maxInactiveIntervalInSeconds = 604800)
@CrossOrigin
@Transactional
public class BaseSecureController {
    public static final int SERVER_ID = ConfigLoader.get("server.id", Integer.class);
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseSecureController.class);
    EntityManager entityManager = new EntityManager();
    @Autowired
    PlayerService playerService;
    @Autowired
    AdminActionLogsService adminActionLogsService;
    @Autowired
    LayersHistoryService layersHistoryService;
    @Autowired
    PlayerBanService playerBanService;
    @Autowired
    PlayerKickService playerKickService;
    @Autowired
    PlayerMessageService playerMessageService;
    @Autowired
    PlayerNoteService playerNoteService;
    @Autowired
    AdminService adminService;
    @Autowired
    RoleGroupService roleGroupService;
    @Autowired
    RoleService roleService;
    @Autowired
    RolesService rolesService;
    @Autowired
    RuleGroupService ruleGroupService;
    @Autowired
    MapService mapService;
    @Autowired
    VehicleService vehicleService;
    @Autowired
    RotationGroupService rotationGroupService;
    @Autowired
    RotationMapService rotationMapService;
    @Autowired
    ServersService serversService;
    @Autowired
    FindByIndexNameSessionRepository<? extends Session> sessions;


    protected <T> HashMap<String, Object> getMapForPagination(Page<T> page) {
        return new HashMap<>() {{
            put("currentPage", page.getNumber());
            put("totalPages", page.getTotalPages());
            put("totalElements", page.getTotalElements());
            put("hasNext", page.hasNext());
            put("nextPage", page.hasNext() ? page.nextPageable().getPageNumber() : null);
            put("hasPrevious", page.hasPrevious());
            put("previousPage", page.hasPrevious() ? page.previousPageable().getPageNumber() : null);
        }};
    }

    protected HashMap<String, Object> getMapForBans(Page<PlayerBanEntity> page) {
        HashMap<String, Object> map = getMapForPagination(page);
        List<LinkedHashMap<String, Object>> contentList = new ArrayList<>();

        page.getContent().forEach(ban -> contentList.add(
                new LinkedHashMap<>() {{
                    put("id", ban.getId());
                    put("bannedPlayer", new HashMap<>() {{
                        put("playerName", ban.getPlayer().getName());
                        put("steamId", ban.getPlayer().getSteamId().toString());
                    }});
                    put("bannedBy", new HashMap<>() {{
                        put("adminName", ban.getAdmin().getName());
                        put("steamId", ban.getAdmin().getSteamId().toString());
                    }});
                    put("reason", ban.getReason());
                    put("isUnbannedManual", ban.getIsUnbannedManually());
                    put("unbannedManualBy",
                            ban.getIsUnbannedManually() ?
                                    new HashMap<>() {{
                                        put("adminName", ban.getUnbannedAdmin().getName());
                                        put("steamId", ban.getUnbannedAdmin().getSteamId().toString());
                                    }}
                                    : null
                    );
                    put("manualUnbannedTime", ban.getUnbannedTime());
                    put("expirationTime", ban.getExpirationTime());
                    put("creationTime", ban.getCreationTime());
                }})
        );
        map.put("content", contentList);
        return map;
    }
}
