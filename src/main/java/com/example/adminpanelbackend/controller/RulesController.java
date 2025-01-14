package com.example.adminpanelbackend.controller;


import com.example.adminpanelbackend.Role;
import com.example.adminpanelbackend.db.entity.AdminEntity;
import com.example.adminpanelbackend.db.entity.RuleGroupEntity;
import com.example.adminpanelbackend.model.RuleGroupModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.session.jdbc.config.annotation.web.http.EnableJdbcHttpSession;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.List;

import static com.example.adminpanelbackend.RoleEnum.BASE;
import static com.example.adminpanelbackend.RoleEnum.RULES_MANAGEMENT;

@RestController()
@EnableJdbcHttpSession(maxInactiveIntervalInSeconds = 604800)
@CrossOrigin
public class RulesController extends BaseSecureController {
    private static final Logger LOGGER = LoggerFactory.getLogger(RulesController.class);

    @Role(role = BASE)
    @GetMapping(path = "/get-rules")
    public ResponseEntity<List<RuleGroupEntity>> getRules(
            @SessionAttribute AdminEntity userInfo,
            HttpSession httpSession,
            HttpServletRequest request,
            HttpServletResponse response) {
        LOGGER.debug("Received secured {} request on '{}' with userInfo in cookie '{}'", request.getMethod(), request.getRequestURL(), userInfo);
        return ResponseEntity.ok(ruleGroupService.findAll());
    }

    @Role(role = RULES_MANAGEMENT)
    @PostMapping(path = "/set-rules")
    public ResponseEntity<Void> setRules(
            @SessionAttribute AdminEntity userInfo,
            HttpSession httpSession,
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestBody RuleGroupModel groupModel) {
        LOGGER.debug("Received secured {} request on '{}' with userInfo in cookie '{}'", request.getMethod(), request.getRequestURL(), userInfo);
        groupModel.getRuleGroup().forEach(ruleGroup -> {
            ruleGroup.getRules().forEach(rule -> rule.setRuleGroup(ruleGroup));
        });
        ruleGroupService.deleteAll();
        ruleGroupService.saveAllAndFlush(groupModel.getRuleGroup());
        return ResponseEntity.ok().build();
    }
}
