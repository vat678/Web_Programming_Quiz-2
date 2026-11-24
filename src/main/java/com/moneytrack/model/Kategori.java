package com.moneytrack.model;

public class Kategori {
    private long id;
    private long userId;
    private String nama;
    private String jenis;
    private boolean dipakai;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }
    public String getNama() { return nama; }
    public void setNama(String nama) { this.nama = nama; }
    public String getJenis() { return jenis; }
    public void setJenis(String jenis) { this.jenis = jenis; }
    public boolean isDipakai() { return dipakai; }
    public void setDipakai(boolean dipakai) { this.dipakai = dipakai; }
}
