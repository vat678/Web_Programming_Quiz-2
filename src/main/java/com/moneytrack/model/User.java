package com.moneytrack.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class User {
    private long id;
    private String nama;
    private String email;
    private String passwordHash;
    private String role;
    private LocalDateTime createdAt;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getNama() { return nama; }
    public void setNama(String nama) { this.nama = nama; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    /** Mis. "24 November 2026" (JSTL fmt:formatDate tidak mendukung LocalDateTime). */
    public String getCreatedAtFormatted() {
        return createdAt == null ? "-"
                : createdAt.format(DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.forLanguageTag("id-ID")));
    }

    public boolean isAdmin() { return "admin".equals(role); }

    public SessionUser toSessionUser() {
        return new SessionUser(id, nama, email, role);
    }
}
