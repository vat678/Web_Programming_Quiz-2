package com.moneytrack.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

/** Hutang (saya berutang) atau piutang (orang lain berutang kepada saya). */
public class HutangPiutang {
    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.forLanguageTag("id-ID"));

    private long id;
    private long userId;
    private String jenis;             // hutang | piutang
    private String namaPihak;
    private BigDecimal nominal;
    private BigDecimal terbayar = BigDecimal.ZERO;
    private LocalDate jatuhTempo;     // boleh null
    private String catatan;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }
    public String getJenis() { return jenis; }
    public void setJenis(String jenis) { this.jenis = jenis; }
    public String getNamaPihak() { return namaPihak; }
    public void setNamaPihak(String namaPihak) { this.namaPihak = namaPihak; }
    public BigDecimal getNominal() { return nominal; }
    public void setNominal(BigDecimal nominal) { this.nominal = nominal; }
    public BigDecimal getTerbayar() { return terbayar; }
    public void setTerbayar(BigDecimal terbayar) { this.terbayar = terbayar == null ? BigDecimal.ZERO : terbayar; }
    public LocalDate getJatuhTempo() { return jatuhTempo; }
    public void setJatuhTempo(LocalDate jatuhTempo) { this.jatuhTempo = jatuhTempo; }
    public String getCatatan() { return catatan; }
    public void setCatatan(String catatan) { this.catatan = catatan; }

    public String getJenisLabel() { return "hutang".equals(jenis) ? "Hutang" : "Piutang"; }

    public BigDecimal getSisa() {
        BigDecimal s = nominal.subtract(terbayar);
        return s.signum() < 0 ? BigDecimal.ZERO : s;
    }

    public boolean isLunas() { return terbayar.compareTo(nominal) >= 0; }

    public int getPersen() {
        if (nominal == null || nominal.signum() == 0) return 0;
        return terbayar.multiply(BigDecimal.valueOf(100)).divide(nominal, 0, RoundingMode.DOWN).intValue();
    }

    public int getPersenBar() { return Math.min(100, Math.max(0, getPersen())); }

    public String getJatuhTempoFormatted() { return jatuhTempo == null ? "-" : jatuhTempo.format(FMT); }
    public String getJatuhTempoIso() { return jatuhTempo == null ? "" : jatuhTempo.toString(); }

    /** Keterangan jatuh tempo, mis. "5 hari lagi" atau "terlambat 3 hari". */
    public String getJatuhTempoInfo() {
        if (jatuhTempo == null || isLunas()) return "";
        long hari = ChronoUnit.DAYS.between(LocalDate.now(), jatuhTempo);
        if (hari > 0) return hari + " hari lagi";
        if (hari == 0) return "hari ini";
        return "terlambat " + (-hari) + " hari";
    }

    /** lunas / terlambat / berjalan. */
    public String getStatus() {
        if (isLunas()) return "lunas";
        if (jatuhTempo != null && jatuhTempo.isBefore(LocalDate.now())) return "terlambat";
        return "berjalan";
    }

    public String getStatusLabel() {
        return switch (getStatus()) {
            case "lunas" -> "Lunas";
            case "terlambat" -> "Lewat jatuh tempo";
            default -> "Belum lunas";
        };
    }
}
