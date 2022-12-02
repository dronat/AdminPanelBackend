package com.example.adminpanelbackend.controller;


import com.example.adminpanelbackend.dataBase.entity.AdminEntity;
import com.example.adminpanelbackend.dataBase.entity.RuleGroupEntity;
import com.example.adminpanelbackend.dataBase.service.RuleGroupService;
import com.woop.Squad4J.a2s.Query;
import com.woop.Squad4J.server.RconUpdater;
import com.woop.Squad4J.server.tailer.FtpBanService;
import com.woop.Squad4J.server.tailer.FtpLogTailer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.session.jdbc.config.annotation.web.http.EnableJdbcHttpSession;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

@RestController()
@EnableJdbcHttpSession(maxInactiveIntervalInSeconds = 604800)
@CrossOrigin
public class RulesController extends BaseSecureController{
    private static final Logger LOGGER = LoggerFactory.getLogger(RulesController.class);
    
    @Autowired
    RuleGroupService ruleGroupService;

    @GetMapping(path = "/get-rules")
    public ResponseEntity<List<RuleGroupEntity>> getRules(
            @SessionAttribute AdminEntity userInfo,
            HttpSession httpSession,
            HttpServletRequest request,
            HttpServletResponse response) {
        LOGGER.debug("Received secured {} request on '{}' with userInfo in cookie '{}'", request.getMethod(), request.getRequestURL(), userInfo);
        return ResponseEntity.ok(ruleGroupService.findAll());
        
    }

    @PostMapping(path = "/set-rules")
    public ResponseEntity<Void> setRules(
            @SessionAttribute AdminEntity userInfo,
            HttpSession httpSession,
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestBody LinkedList<RuleGroupEntity> ruleGroups) {
        LOGGER.debug("Received secured {} request on '{}' with userInfo in cookie '{}'", request.getMethod(), request.getRequestURL(), userInfo);
        System.out.println();
        ruleGroups.forEach(ruleGroup -> {
            ruleGroup.getRules().forEach(rule -> rule.setRuleGroup(ruleGroup));
        });
        ruleGroupService.deleteAll();
        ruleGroupService.saveAllAndFlush(ruleGroups);
        return ResponseEntity.ok().build();
    }
}
