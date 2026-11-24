package com.moneytrack.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/** Memaksa UTF-8 dan menambahkan header keamanan dasar. */
public class EncodingFilter implements Filter {
    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
            throws IOException, ServletException {
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        if (resp instanceof HttpServletResponse r) {
            r.setHeader("X-Content-Type-Options", "nosniff");
            r.setHeader("X-Frame-Options", "DENY");
            r.setHeader("Referrer-Policy", "same-origin");
        }
        chain.doFilter(req, resp);
    }
}
