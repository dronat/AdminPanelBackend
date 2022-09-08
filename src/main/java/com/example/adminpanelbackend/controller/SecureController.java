package com.example.adminpanelbackend.controller;

import com.example.adminpanelbackend.dataBase.AdminsEntityManager;
import com.example.adminpanelbackend.repository.Admins;
import com.woop.Squad4J.model.OnlineInfo;
import com.woop.Squad4J.server.SquadServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.session.jdbc.config.annotation.web.http.EnableJdbcHttpSession;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@RestController()
@EnableJdbcHttpSession(maxInactiveIntervalInSeconds = 604800)
@CrossOrigin
//@SessionAttributes("userInfo")
public class SecureController {
    private static final Logger LOGGER = LoggerFactory.getLogger(NotSecureController.class);
    AdminsEntityManager adminsEntityManager = new AdminsEntityManager();

    @GetMapping(path = "/get-online")
    public ResponseEntity<OnlineInfo> getPlayersAndSquads(@SessionAttribute Admins userInfo,
                                                          HttpSession httpSession,
                                                          HttpServletRequest request,
                                                          HttpServletResponse response) {
        LOGGER.debug("Received secured GET request on '{}' with userInfo in cookie '{}'", request.getRequestURL(), userInfo);
        return ResponseEntity.ok(SquadServer.getTeamsWithSquadsAndPlayers());
    }

    @GetMapping(path = "/auth")
    public ResponseEntity<Void> auth(@SessionAttribute Admins userInfo,
                                     HttpSession httpSession,
                                     HttpServletRequest request,
                                     HttpServletResponse response) {
        LOGGER.debug("Received secured GET request on '{}' with userInfo in cookie '{}'", request.getRequestURL(), userInfo);
        return adminsEntityManager.getAdminBySteamID(String.valueOf(userInfo.getSteamId())) == null
                ? ResponseEntity.ok(null)
                : ResponseEntity.status(401).build();
    }

    @GetMapping(path = "/logout")
    public ResponseEntity<Void> logout(@SessionAttribute Admins userInfo, HttpSession session,
                                       HttpSession httpSession,
                                       HttpServletRequest request,
                                       HttpServletResponse response) {
        LOGGER.debug("Received secured GET request on '{}' with userInfo in cookie '{}'", request.getRequestURL(), userInfo);
        session.invalidate();
        return adminsEntityManager.getAdminBySteamID(String.valueOf(userInfo.getSteamId())) != null
                ? ResponseEntity.status(200).build()
                : ResponseEntity.status(401).build();
    }
}
