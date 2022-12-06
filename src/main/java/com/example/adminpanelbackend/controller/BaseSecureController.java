package com.example.adminpanelbackend.controller;

import com.example.adminpanelbackend.dataBase.EntityManager;
import com.example.adminpanelbackend.dataBase.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;
import org.springframework.session.jdbc.config.annotation.web.http.EnableJdbcHttpSession;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;

@RestController()
@EnableJdbcHttpSession(maxInactiveIntervalInSeconds = 604800)
@CrossOrigin
public class BaseSecureController {
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
}
