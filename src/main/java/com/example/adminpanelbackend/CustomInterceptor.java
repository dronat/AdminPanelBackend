package com.example.adminpanelbackend;

import com.example.adminpanelbackend.controller.NotSecureController;
import com.example.adminpanelbackend.dataBase.EntityManager;
import com.example.adminpanelbackend.dataBase.entity.AdminEntity;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class CustomInterceptor implements HandlerInterceptor {
    EntityManager em = new EntityManager();
    private static final Logger LOGGER = LoggerFactory.getLogger(CustomInterceptor.class);

    @Override
    public boolean preHandle(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull Object handler) {
        try {
            if (request.getMethod().equals("OPTIONS")) {
                return true;
            }
            if (((HandlerMethod) handler).getMethod().getDeclaringClass().equals(NotSecureController.class)) {
                return true;
            }
            Role methodRole = ((HandlerMethod) handler).getMethod().getDeclaredAnnotation(Role.class);
            if (methodRole == null) {
                response.setStatus(401);
                return false;
            }

            AdminEntity admin = (AdminEntity) request.getSession().getAttribute("userInfo");
            if (admin == null) {
                response.setStatus(401);

                return false;
            }

            admin = em.tryGetAdminBySteamID(admin.getSteamId());
            if (admin == null || admin.getRoleGroup() == null || admin.getRoleGroup().getRoles() == null || admin.getRoleGroup().getRoles().isEmpty()) {
                response.setStatus(401);
                return false;
            }

            if (admin.getRoleGroup().getRoles().stream().noneMatch(adminRole -> adminRole.getRole().getName().equals(methodRole.role().name))) {
                response.setStatus(401);
                return false;
            }
            return true;
        } catch (Exception e) {
            response.setStatus(401);
            LOGGER.warn("Unauthorized request on '" + request.getServletPath());
            return false;
        }
    }
}
