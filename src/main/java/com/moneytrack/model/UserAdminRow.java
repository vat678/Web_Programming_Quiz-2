package com.moneytrack.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class UserAdminRow {
    private long id;
    private String nama;
    private String email;
    private String role;
    private LocalDateTime createdAt;
    private long jumlahDompet;
    private long jumlahTransaksi;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getNama() { return nama; }
    public void setNama(String nama) { this.nama = nama; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public long getJumlahDompet() { return jumlahDompet; }
    public void setJumlahDompet(long jumlahDompet) { this.jumlahDompet = jumlahDompet; }
    public long getJumlahTransaksi() { return jumlahTransaksi; }
    public void setJumlahTransaksi(long jumlahTransaksi) { this.jumlahTransaksi = jumlahTransaksi; }

    public boolean isAdmin() { return "admin".equals(role); }

    public String getCreatedAtFormatted() {
        return createdAt == null ? "-"
                : createdAt.format(DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.forLanguageTag("id-ID")));
    }
}