package com.moneytrack.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

/** Template transaksi rutin (gaji bulanan, tagihan, langganan, dll). */
public class TransaksiBerulang {
    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.forLanguageTag("id-ID"));

    private long id;
    private long userId;
    private long dompetId;
    private Long kategoriId;          // boleh null
    private String jenis;
    private String judul;
    private BigDecimal nominal;
    private String frekuensi;         // harian | mingguan | bulanan | tahunan
    private LocalDate tanggalBerikutnya;
    private boolean aktif = true;
    private String dompetNama;        // hasil JOIN
    private String kategoriNama;      // hasil JOIN

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
    public String getFrekuensi() { return frekuensi; }
    public void setFrekuensi(String frekuensi) { this.frekuensi = frekuensi; }
    public LocalDate getTanggalBerikutnya() { return tanggalBerikutnya; }
    public void setTanggalBerikutnya(LocalDate tanggalBerikutnya) { this.tanggalBerikutnya = tanggalBerikutnya; }
    public boolean isAktif() { return aktif; }
    public void setAktif(boolean aktif) { this.aktif = aktif; }
    public String getDompetNama() { return dompetNama; }
    public void setDompetNama(String dompetNama) { this.dompetNama = dompetNama; }
    public String getKategoriNama() { return kategoriNama; }
    public void setKategoriNama(String kategoriNama) { this.kategoriNama = kategoriNama; }

    public String getFrekuensiLabel() {
        return switch (frekuensi == null ? "" : frekuensi) {
            case "harian" -> "Setiap hari";
            case "mingguan" -> "Setiap minggu";
            case "tahunan" -> "Setiap tahun";
            default -> "Setiap bulan";
        };
    }

    public String getTanggalBerikutnyaFormatted() { return tanggalBerikutnya == null ? "" : tanggalBerikutnya.format(FMT); }
    public String getTanggalBerikutnyaIso() { return tanggalBerikutnya == null ? "" : tanggalBerikutnya.toString(); }

    /** Aktif dan tanggalnya sudah tiba / lewat, sehingga siap diproses menjadi transaksi. */
    public boolean isJatuhTempo() {
        return aktif && tanggalBerikutnya != null && !tanggalBerikutnya.isAfter(LocalDate.now());
    }

    /** Keterangan relatif, mis. "3 hari lagi" atau "terlambat 2 hari". */
    public String getTanggalInfo() {
        if (!aktif || tanggalBerikutnya == null) return "";
        long hari = ChronoUnit.DAYS.between(LocalDate.now(), tanggalBerikutnya);
        if (hari > 0) return hari + " hari lagi";
        if (hari == 0) return "hari ini";
        return "terlambat " + (-hari) + " hari";
    }

    /** Menghitung tanggal kejadian berikutnya setelah {@code dari} sesuai frekuensi. */
    public static LocalDate berikutnya(LocalDate dari, String frekuensi) {
        return switch (frekuensi) {
            case "harian" -> dari.plusDays(1);
            case "mingguan" -> dari.plusWeeks(1);
            case "tahunan" -> dari.plusYears(1);
            default -> dari.plusMonths(1);
        };
    }
}
