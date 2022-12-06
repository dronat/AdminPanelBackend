package com.example.adminpanelbackend.controller;


import com.example.adminpanelbackend.Role;
import com.example.adminpanelbackend.dataBase.entity.AdminEntity;
import com.example.adminpanelbackend.dataBase.entity.RoleEntity;
import com.example.adminpanelbackend.dataBase.entity.RoleGroupEntity;
import com.example.adminpanelbackend.dataBase.entity.RolesEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.session.jdbc.config.annotation.web.http.EnableJdbcHttpSession;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Optional;

import static com.example.adminpanelbackend.RoleEnum.BASE;
import static com.example.adminpanelbackend.RoleEnum.ROLES_MANAGEMENT;

@RestController
@EnableJdbcHttpSession(maxInactiveIntervalInSeconds = 604800)
@CrossOrigin
public class RolesController extends BaseSecureController {
    private static final Logger LOGGER = LoggerFactory.getLogger(RolesController.class);

    @Role(role = BASE)
    @GetMapping(path = "/get-my-role-group")
    public ResponseEntity<RoleGroupEntity> getMyRoleGroup(
            @SessionAttribute AdminEntity userInfo,
            HttpSession httpSession,
            HttpServletRequest request,
            HttpServletResponse response) {
        LOGGER.debug("Received secured {} request on '{}' with userInfo in cookie '{}'", request.getMethod(), request.getRequestURL(), userInfo);
        return ResponseEntity.ok(adminService.findById(userInfo.getSteamId()).get().getRoleGroup());
    }

    @Role(role = ROLES_MANAGEMENT)
    @GetMapping(path = "/get-role-group")
    public ResponseEntity<RoleGroupEntity> getRoleGroup(
            @RequestParam int roleGroupId,
            @SessionAttribute AdminEntity userInfo,
            HttpSession httpSession,
            HttpServletRequest request,
            HttpServletResponse response) {
        LOGGER.debug("Received secured {} request on '{}' with userInfo in cookie '{}'", request.getMethod(), request.getRequestURL(), userInfo);
        Optional<RoleGroupEntity> roleGroup = roleGroupService.findById(roleGroupId);
        return roleGroup.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @Role(role = ROLES_MANAGEMENT)
    @GetMapping(path = "/get-all-role-groups")
    public ResponseEntity<List<RoleGroupEntity>> getAllRoleGroups(
            @SessionAttribute AdminEntity userInfo,
            HttpSession httpSession,
            HttpServletRequest request,
            HttpServletResponse response) {
        LOGGER.debug("Received secured {} request on '{}' with userInfo in cookie '{}'", request.getMethod(), request.getRequestURL(), userInfo);
        return ResponseEntity.ok(roleGroupService.findAll());
    }

    @Role(role = ROLES_MANAGEMENT)
    @GetMapping(path = "/get-role")
    public ResponseEntity<RoleEntity> getRole(
            @RequestParam int roleId,
            @SessionAttribute AdminEntity userInfo,
            HttpSession httpSession,
            HttpServletRequest request,
            HttpServletResponse response) {
        LOGGER.debug("Received secured {} request on '{}' with userInfo in cookie '{}'", request.getMethod(), request.getRequestURL(), userInfo);
        Optional<RoleEntity> roleGroup = roleService.findById(roleId);
        return roleGroup.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @Role(role = ROLES_MANAGEMENT)
    @GetMapping(path = "/get-all-roles")
    public ResponseEntity<List<RoleEntity>> getAllRoles(
            @SessionAttribute AdminEntity userInfo,
            HttpSession httpSession,
            HttpServletRequest request,
            HttpServletResponse response) {
        LOGGER.debug("Received secured {} request on '{}' with userInfo in cookie '{}'", request.getMethod(), request.getRequestURL(), userInfo);
        return ResponseEntity.ok(roleService.findAll());
    }

    @Role(role = ROLES_MANAGEMENT)
    @PostMapping(path = "/add-role-in-role-group")
    public ResponseEntity<Void> addRoleInRoleGroup(
            @RequestParam RolesEntity rolesEntity,
            @SessionAttribute AdminEntity userInfo,
            HttpSession httpSession,
            HttpServletRequest request,
            HttpServletResponse response) {
        LOGGER.debug("Received secured {} request on '{}' with userInfo in cookie '{}'", request.getMethod(), request.getRequestURL(), userInfo);
        rolesService.saveAndFlush(rolesEntity);
        return ResponseEntity.ok().build();
    }

    @Role(role = ROLES_MANAGEMENT)
    @PostMapping(path = "/add-role-group")
    public ResponseEntity<Void> addRoleGroup(
            @RequestParam RoleGroupEntity rolesEntity,
            @SessionAttribute AdminEntity userInfo,
            HttpSession httpSession,
            HttpServletRequest request,
            HttpServletResponse response) {
        LOGGER.debug("Received secured {} request on '{}' with userInfo in cookie '{}'", request.getMethod(), request.getRequestURL(), userInfo);
        roleGroupService.saveAndFlush(rolesEntity);
        return ResponseEntity.ok().build();
    }

    @Role(role = ROLES_MANAGEMENT)
    @PostMapping(path = "/remove-role-group")
    public ResponseEntity<Void> removeRoleGroup(
            @RequestParam int roleGroupId,
            @SessionAttribute AdminEntity userInfo,
            HttpSession httpSession,
            HttpServletRequest request,
            HttpServletResponse response) {
        LOGGER.debug("Received secured {} request on '{}' with userInfo in cookie '{}'", request.getMethod(), request.getRequestURL(), userInfo);
        roleGroupService.deleteById(roleGroupId);
        return ResponseEntity.ok().build();
    }

    @Role(role = ROLES_MANAGEMENT)
    @PostMapping(path = "/remove-role-from-role-group")
    public ResponseEntity<Void> removeRoleFromRoleGroup(
            @RequestParam int roleId,
            @SessionAttribute AdminEntity userInfo,
            HttpSession httpSession,
            HttpServletRequest request,
            HttpServletResponse response) {
        LOGGER.debug("Received secured {} request on '{}' with userInfo in cookie '{}'", request.getMethod(), request.getRequestURL(), userInfo);
        Optional<RoleGroupEntity> roleGroup = roleGroupService.findById(roleId);
        if (roleGroup.isPresent()) {
            roleGroup.get().getRoles().removeIf(role -> role.getRole().getId().equals(roleId));
            roleGroupService.saveAndFlush(roleGroup.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok().build();
    }

    @Role(role = ROLES_MANAGEMENT)
    @PostMapping(path = "/set-role-group-to-admin")
    public ResponseEntity<Void> setRoleGroupToAdmin(
            @RequestParam String adminSteamId,
            @RequestParam int roleGroupId,
            @SessionAttribute AdminEntity userInfo,
            HttpSession httpSession,
            HttpServletRequest request,
            HttpServletResponse response) {
        LOGGER.debug("Received secured {} request on '{}' with userInfo in cookie '{}'", request.getMethod(), request.getRequestURL(), userInfo);
        Optional<AdminEntity> admin = adminService.findById(Long.parseLong(adminSteamId));
        Optional<RoleGroupEntity> roleGroup = roleGroupService.findById(roleGroupId);
        if (admin.isEmpty() || roleGroup.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        admin.get().setRoleGroup(roleGroup.get());
        adminService.saveAndFlush(admin.get());
        return ResponseEntity.ok().build();
    }
}
