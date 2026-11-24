package com.moneytrack.filter;

import com.moneytrack.model.SessionUser;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/** Pengganti middleware auth/guest Laravel, plus proteksi /admin/* hanya untuk role admin. */
public class AuthFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        String path = req.getRequestURI().substring(req.getContextPath().length());
        HttpSession session = req.getSession(false);
        SessionUser user = session == null ? null : (SessionUser) session.getAttribute("user");

        boolean asset = path.startsWith("/assets/");
        boolean guestPage = path.equals("/login") || path.equals("/register");

        if (asset) {
            chain.doFilter(request, response);
            return;
        }
        if (guestPage) {
            if (user != null) {
                resp.sendRedirect(req.getContextPath() + "/");
                return;
            }
            chain.doFilter(request, response);
            return;
        }
        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        if (path.equals("/admin") || path.startsWith("/admin/")) {
            if (!user.isAdmin()) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
        }
        chain.doFilter(request, response);
    }
}
