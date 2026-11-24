package com.moneytrack.util;

import jakarta.servlet.http.HttpServletRequest;

/** Pesan flash disimpan di session dan dihapus setelah ditampilkan oleh layout. */
public final class Flash {
    private Flash() {}

    public static void success(HttpServletRequest req, String msg) {
        req.getSession(true).setAttribute("flash_success", msg);
    }

    public static void error(HttpServletRequest req, String msg) {
        req.getSession(true).setAttribute("flash_error", msg);
    }
}
