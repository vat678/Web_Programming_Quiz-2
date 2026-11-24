package com.moneytrack.util;

import jakarta.servlet.http.HttpServletRequest;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.Map;

/** Pembantu validasi form manual (pengganti $request->validate() di Laravel). */
public class Form {
    private static final BigDecimal MAX_MONEY = new BigDecimal("9999999999999.99");

    private final HttpServletRequest req;
    private final Map<String, String> errors = new LinkedHashMap<>();

    public Form(HttpServletRequest req) {
        this.req = req;
    }

    public String str(String name) {
        String v = req.getParameter(name);
        return v == null ? "" : v.trim();
    }

    public String required(String name, String label, int max) {
        String v = str(name);
        if (v.isEmpty()) error(name, label + " wajib diisi.");
        else if (v.length() > max) error(name, label + " maksimal " + max + " karakter.");
        return v;
    }

    public String optional(String name, String label, int max) {
        String v = str(name);
        if (v.length() > max) error(name, label + " maksimal " + max + " karakter.");
        return v.isEmpty() ? null : v;
    }

    public String oneOf(String name, String label, String... allowed) {
        String v = str(name);
        if (v.isEmpty()) {
            error(name, label + " wajib dipilih.");
            return null;
        }
        for (String a : allowed) if (a.equals(v)) return v;
        error(name, label + " tidak valid.");
        return null;
    }

    /** Nominal uang: harus angka, > 0 (atau >= 0 jika allowZero), maksimal 9.999.999.999.999,99. */
    public BigDecimal money(String name, String label, boolean allowZero) {
        String v = str(name);
        if (v.isEmpty()) {
            error(name, label + " wajib diisi.");
            return null;
        }
        try {
            BigDecimal d = new BigDecimal(v.replace(",", "."));
            if (d.signum() < 0 || (!allowZero && d.signum() == 0)) {
                error(name, label + (allowZero ? " tidak boleh negatif." : " harus lebih besar dari nol."));
                return null;
            }
            if (d.compareTo(MAX_MONEY) > 0) {
                error(name, label + " terlalu besar.");
                return null;
            }
            return d.setScale(2, RoundingMode.HALF_UP);
        } catch (NumberFormatException e) {
            error(name, label + " harus berupa angka.");
            return null;
        }
    }

    public LocalDate date(String name, String label, boolean required) {
        String v = str(name);
        if (v.isEmpty()) {
            if (required) error(name, label + " wajib diisi.");
            return null;
        }
        try {
            return LocalDate.parse(v);
        } catch (DateTimeParseException e) {
            error(name, label + " tidak valid.");
            return null;
        }
    }

    public Integer integer(String name, String label, int min, int max) {
        String v = str(name);
        if (v.isEmpty()) {
            error(name, label + " wajib diisi.");
            return null;
        }
        try {
            int n = Integer.parseInt(v);
            if (n < min || n > max) {
                error(name, label + " harus antara " + min + " sampai " + max + ".");
                return null;
            }
            return n;
        } catch (NumberFormatException e) {
            error(name, label + " harus berupa angka bulat.");
            return null;
        }
    }

    /** Parameter id (Long); kembalikan null jika tidak ada atau tidak valid. */
    public static Long id(HttpServletRequest req, String name) {
        try {
            String v = req.getParameter(name);
            return v == null ? null : Long.valueOf(v.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public void error(String field, String msg) {
        errors.putIfAbsent(field, msg);
    }

    public boolean ok() {
        return errors.isEmpty();
    }

    /** Menaruh daftar error ke request agar bisa dibaca JSP lewat ${errors}. */
    public void publish() {
        req.setAttribute("errors", errors);
    }
}
