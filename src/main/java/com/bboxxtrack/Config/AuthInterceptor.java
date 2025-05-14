package com.bboxxtrack.Config;

import com.bboxxtrack.Model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest req,
                             HttpServletResponse res,
                             Object handler) throws Exception {
        User u = (User) req.getSession().getAttribute("user");
        String path = req.getRequestURI();

        // allow login, static resources, calendar.ics, API endpoints
        if (path.startsWith("/login")
                || path.startsWith("/css")
                || path.startsWith("/js")
                || path.startsWith("/calendar.ics")
                || path.startsWith("/api/")) {
            return true;
        }
        if (u == null) {
            res.sendRedirect("/login");
            return false;
        }
        // you can add role-based checks here if desired
        return true;
    }
}
