package com.example.adminpanelbackend.controller;


import com.example.adminpanelbackend.Role;
import com.example.adminpanelbackend.dataBase.entity.AdminEntity;
import com.woop.Squad4J.a2s.Query;
import com.woop.Squad4J.server.RconUpdater;
import com.woop.Squad4J.server.tailer.FtpBanService;
import com.woop.Squad4J.server.tailer.FtpLogTailer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.session.jdbc.config.annotation.web.http.EnableJdbcHttpSession;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.HashMap;

import static com.example.adminpanelbackend.RoleEnum.BASE;

@RestController()
@EnableJdbcHttpSession(maxInactiveIntervalInSeconds = 604800)
@CrossOrigin
public class InfoController extends BaseSecureController{
    private static final Logger LOGGER = LoggerFactory.getLogger(InfoController.class);


    @Role(role = BASE)
    @GetMapping(path = "/get-backend-status")
    public ResponseEntity<HashMap<String, Object>> getBackendStatus(@SessionAttribute AdminEntity userInfo, HttpSession httpSession, HttpServletRequest request, HttpServletResponse response) {
        LOGGER.debug("Received secured {} request on '{}' with userInfo in cookie '{}'", request.getMethod(), request.getRequestURL(), userInfo);
        return ResponseEntity.ok(new HashMap<>() {{
            put("RconUpdater", RconUpdater.lastSuccessfullyWork);
            put("QueryUpdater", Query.lastSuccessfullyWork);
            put("FtpLogTailer", FtpLogTailer.lastSuccessfullyWork);
            put("FtpBanService", FtpBanService.lastSuccessfullyWork);
        }});
    }

}
