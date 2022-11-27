package com.example.adminpanelbackend;

import org.jetbrains.annotations.NotNull;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

public class COOKIEFilter implements HandlerInterceptor {

    @Override
    public boolean preHandle(@NotNull HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o) throws Exception {

        Cookie cookie = new Cookie("timestamp", Long.toString(new Date().getTime()));
        cookie.setHttpOnly(false);
        cookie.setMaxAge(1 * 86400);
        httpServletResponse.addCookie(cookie);
        return true;
    }
}
