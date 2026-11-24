package com.moneytrack.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

public class TargetTabungan {
    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.forLanguageTag("id-ID"));

    private long id;
    private long userId;
    private String nama;
    private BigDecimal targetNominal;
    private BigDecimal terkumpul = BigDecimal.ZERO;
    private LocalDate tenggat;        // boleh null
    private String catatan;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }
    public String getNama() { return nama; }
    public void setNama(String nama) { this.nama = nama; }
    public BigDecimal getTargetNominal() { return targetNominal; }
    public void setTargetNominal(BigDecimal targetNominal) { this.targetNominal = targetNominal; }
    public BigDecimal getTerkumpul() { return terkumpul; }
    public void setTerkumpul(BigDecimal terkumpul) { this.terkumpul = terkumpul == null ? BigDecimal.ZERO : terkumpul; }
    public LocalDate getTenggat() { return tenggat; }
    public void setTenggat(LocalDate tenggat) { this.tenggat = tenggat; }
    public String getCatatan() { return catatan; }
    public void setCatatan(String catatan) { this.catatan = catatan; }

    public BigDecimal getSisa() {
        BigDecimal s = targetNominal.subtract(terkumpul);
        return s.signum() < 0 ? BigDecimal.ZERO : s;
    }

    public boolean isTercapai() { return terkumpul.compareTo(targetNominal) >= 0; }

    public int getPersen() {
        if (targetNominal == null || targetNominal.signum() == 0) return 0;
        return terkumpul.multiply(BigDecimal.valueOf(100)).divide(targetNominal, 0, RoundingMode.DOWN).intValue();
    }

    /** Lebar progress bar, dibatasi 0-100. */
    public int getPersenBar() { return Math.min(100, Math.max(0, getPersen())); }

    public String getTenggatFormatted() { return tenggat == null ? "" : tenggat.format(FMT); }
    public String getTenggatIso() { return tenggat == null ? "" : tenggat.toString(); }

    /** Keterangan tenggat untuk tampilan kartu. */
    public String getTenggatInfo() {
        if (tenggat == null) return "Tanpa tenggat";
        if (isTercapai()) return "Tenggat " + getTenggatFormatted();
        long hari = ChronoUnit.DAYS.between(LocalDate.now(), tenggat);
        if (hari > 0) return getTenggatFormatted() + " · " + hari + " hari lagi";
        if (hari == 0) return getTenggatFormatted() + " · hari ini";
        return getTenggatFormatted() + " · terlambat " + (-hari) + " hari";
    }

    /** tercapai / terlambat / berjalan — dipakai untuk warna lencana. */
    public String getStatus() {
        if (isTercapai()) return "tercapai";
        if (tenggat != null && tenggat.isBefore(LocalDate.now())) return "terlambat";
        return "berjalan";
    }

    public String getStatusLabel() {
        return switch (getStatus()) {
            case "tercapai" -> "Tercapai";
            case "terlambat" -> "Lewat tenggat";
            default -> "Berjalan";
        };
    }
}
