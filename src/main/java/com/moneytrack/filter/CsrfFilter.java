package com.moneytrack.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/** Token CSRF per sesi. Semua request selain GET/HEAD/OPTIONS wajib membawa parameter "_csrf" yang cocok. */
public class CsrfFilter implements Filter {
    public static final String ATTR = "csrfToken";
    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        HttpSession session = req.getSession(true);
        String token = (String) session.getAttribute(ATTR);
        if (token == null) {
            byte[] b = new byte[32];
            RANDOM.nextBytes(b);
            token = Base64.getUrlEncoder().withoutPadding().encodeToString(b);
            session.setAttribute(ATTR, token);
        }
        req.setAttribute(ATTR, token);

        String m = req.getMethod();
        boolean safe = "GET".equals(m) || "HEAD".equals(m) || "OPTIONS".equals(m);
        if (!safe) {
            String sent = req.getParameter("_csrf");
            boolean valid = sent != null && MessageDigest.isEqual(
                    sent.getBytes(StandardCharsets.UTF_8), token.getBytes(StandardCharsets.UTF_8));
            if (!valid) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Token CSRF tidak valid.");
                return;
            }
        }
        chain.doFilter(request, response);
    }
}
