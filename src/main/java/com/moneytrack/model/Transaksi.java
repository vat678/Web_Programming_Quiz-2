package com.moneytrack.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class Transaksi {
    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.forLanguageTag("id-ID"));

    private long id;
    private long userId;
    private long dompetId;
    private Long kategoriId;          // boleh null (tanpa kategori)
    private String jenis;
    private String judul;
    private BigDecimal nominal;
    private LocalDate tanggal;
    private String catatan;
    private String kategoriNama;      // hasil JOIN
    private String dompetNama;        // hasil JOIN

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }
    public long getDompetId() { return dompetId; }
    public void setDompetId(long dompetId) { this.dompetId = dompetId; }
    public Long getKategoriId() { return kategoriId; }
    public void setKategoriId(Long kategoriId) { this.kategoriId = kategoriId; }
    public String getJenis() { return jenis; }
    public void setJenis(String jenis) { this.jenis = jenis; }
    public String getJudul() { return judul; }
    public void setJudul(String judul) { this.judul = judul; }
    public BigDecimal getNominal() { return nominal; }
    public void setNominal(BigDecimal nominal) { this.nominal = nominal; }
    public LocalDate getTanggal() { return tanggal; }
    public void setTanggal(LocalDate tanggal) { this.tanggal = tanggal; }
    public String getCatatan() { return catatan; }
    public void setCatatan(String catatan) { this.catatan = catatan; }
    public String getKategoriNama() { return kategoriNama; }
    public void setKategoriNama(String kategoriNama) { this.kategoriNama = kategoriNama; }
    public String getDompetNama() { return dompetNama; }
    public void setDompetNama(String dompetNama) { this.dompetNama = dompetNama; }

    /** Untuk tampilan, mis. "24 Nov 2026" (JSTL fmt:formatDate tidak mendukung LocalDate). */
    public String getTanggalFormatted() { return tanggal == null ? "" : tanggal.format(FMT); }

    /** Untuk value input type="date". */
    public String getTanggalIso() { return tanggal == null ? "" : tanggal.toString(); }
}
