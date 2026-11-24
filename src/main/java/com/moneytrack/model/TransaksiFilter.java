package com.moneytrack.model;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

/** Kriteria pencarian/filter daftar transaksi. Nilai tidak valid sudah dibersihkan oleh servlet. */
public class TransaksiFilter {
    private String q = "";
    private String jenis = "";
    private Long kategoriId;
    private Long dompetId;
    private LocalDate dari;
    private LocalDate sampai;

    public String getQ() { return q; }
    public void setQ(String q) { this.q = q == null ? "" : q; }
    public String getJenis() { return jenis; }
    public void setJenis(String jenis) { this.jenis = jenis == null ? "" : jenis; }
    public Long getKategoriId() { return kategoriId; }
    public void setKategoriId(Long kategoriId) { this.kategoriId = kategoriId; }
    public Long getDompetId() { return dompetId; }
    public void setDompetId(Long dompetId) { this.dompetId = dompetId; }
    public LocalDate getDari() { return dari; }
    public void setDari(LocalDate dari) { this.dari = dari; }
    public LocalDate getSampai() { return sampai; }
    public void setSampai(LocalDate sampai) { this.sampai = sampai; }

    public boolean isAktif() {
        return !q.isEmpty() || !jenis.isEmpty() || kategoriId != null || dompetId != null
                || dari != null || sampai != null;
    }

    /** Query string (sudah di-URL-encode) agar filter terbawa saat pindah halaman. */
    public String getQueryString() {
        StringBuilder sb = new StringBuilder();
        add(sb, "q", q);
        add(sb, "jenis", jenis);
        add(sb, "kategori", kategoriId == null ? "" : kategoriId.toString());
        add(sb, "dompet", dompetId == null ? "" : dompetId.toString());
        add(sb, "dari", dari == null ? "" : dari.toString());
        add(sb, "sampai", sampai == null ? "" : sampai.toString());
        return sb.toString();
    }

    private static void add(StringBuilder sb, String k, String v) {
        if (v == null || v.isEmpty()) return;
        if (sb.length() > 0) sb.append('&');
        sb.append(k).append('=').append(URLEncoder.encode(v, StandardCharsets.UTF_8));
    }
}
