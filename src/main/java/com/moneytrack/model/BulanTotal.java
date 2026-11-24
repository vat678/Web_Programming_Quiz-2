package com.moneytrack.model;

import java.math.BigDecimal;

/** Total pemasukan/pengeluaran satu bulan untuk grafik batang di dashboard. */
public class BulanTotal {
    private final String label;
    private final BigDecimal pemasukan;
    private final BigDecimal pengeluaran;
    private int tinggiPemasukan;     // persen 0-100 relatif terhadap nilai terbesar
    private int tinggiPengeluaran;

    public BulanTotal(String label, BigDecimal pemasukan, BigDecimal pengeluaran) {
        this.label = label;
        this.pemasukan = pemasukan;
        this.pengeluaran = pengeluaran;
    }

    public String getLabel() { return label; }
    public BigDecimal getPemasukan() { return pemasukan; }
    public BigDecimal getPengeluaran() { return pengeluaran; }
    public int getTinggiPemasukan() { return tinggiPemasukan; }
    public void setTinggiPemasukan(int v) { this.tinggiPemasukan = v; }
    public int getTinggiPengeluaran() { return tinggiPengeluaran; }
    public void setTinggiPengeluaran(int v) { this.tinggiPengeluaran = v; }
}
