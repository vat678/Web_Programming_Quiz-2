package com.moneytrack.model;

import java.io.Serializable;

/** Data pengguna yang disimpan di HttpSession (tanpa hash password). */
public class SessionUser implements Serializable {
    private static final long serialVersionUID = 1L;
    private final long id;
    private final String nama;
    private final String email;
    private final String role;

    public SessionUser(long id, String nama, String email, String role) {
        this.id = id;
        this.nama = nama;
        this.email = email;
        this.role = role;
    }

    public long getId() { return id; }
    public String getNama() { return nama; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public boolean isAdmin() { return "admin".equals(role); }
    public String getInisial() { return nama == null || nama.isEmpty() ? "?" : nama.substring(0, 1).toUpperCase(); }
}
