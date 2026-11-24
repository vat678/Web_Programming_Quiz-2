package com.moneytrack.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Anggaran pengeluaran satu kategori pada satu bulan; {@code terpakai} dihitung dari transaksi. */
public class Anggaran {
    private long id;
    private long userId;
    private long kategoriId;
    private String kategoriNama;      // hasil JOIN
    private BigDecimal jumlah;
    private int bulan;
    private int tahun;
    private BigDecimal terpakai = BigDecimal.ZERO;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }
    public long getKategoriId() { return kategoriId; }
    public void setKategoriId(long kategoriId) { this.kategoriId = kategoriId; }
    public String getKategoriNama() { return kategoriNama; }
    public void setKategoriNama(String kategoriNama) { this.kategoriNama = kategoriNama; }
    public BigDecimal getJumlah() { return jumlah; }
    public void setJumlah(BigDecimal jumlah) { this.jumlah = jumlah; }
    public int getBulan() { return bulan; }
    public void setBulan(int bulan) { this.bulan = bulan; }
    public int getTahun() { return tahun; }
    public void setTahun(int tahun) { this.tahun = tahun; }
    public BigDecimal getTerpakai() { return terpakai; }
    public void setTerpakai(BigDecimal terpakai) { this.terpakai = terpakai == null ? BigDecimal.ZERO : terpakai; }

    public BigDecimal getSisa() { return jumlah.subtract(terpakai); }

    /** Persentase terpakai terhadap anggaran (bisa lebih dari 100). */
    public int getPersen() {
        if (jumlah == null || jumlah.signum() == 0) return 0;
        return terpakai.multiply(BigDecimal.valueOf(100)).divide(jumlah, 0, RoundingMode.HALF_UP).intValue();
    }

    /** Lebar progress bar, dibatasi 0-100. */
    public int getPersenBar() { return Math.min(100, Math.max(0, getPersen())); }

    /** aman (&lt;80%), waspada (80-100%), lewat (&gt;100%). */
    public String getStatus() {
        int p = getPersen();
        return p > 100 ? "lewat" : p >= 80 ? "waspada" : "aman";
    }

    public String getStatusLabel() {
        return switch (getStatus()) {
            case "lewat" -> "Melebihi anggaran";
            case "waspada" -> "Hampir habis";
            default -> "Aman";
        };
    }
}
