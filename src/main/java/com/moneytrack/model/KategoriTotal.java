package com.moneytrack.model;

import java.math.BigDecimal;

/** Total pengeluaran per kategori (untuk daftar "Pengeluaran per Kategori"). */
public class KategoriTotal {
    private final String nama;
    private final BigDecimal total;
    private int persen;   // porsi terhadap total pengeluaran, 0-100

    public KategoriTotal(String nama, BigDecimal total) {
        this.nama = nama;
        this.total = total;
    }

    public String getNama() { return nama; }
    public BigDecimal getTotal() { return total; }
    public int getPersen() { return persen; }
    public void setPersen(int persen) { this.persen = persen; }
}
