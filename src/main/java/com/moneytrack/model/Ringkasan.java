package com.moneytrack.model;

import java.math.BigDecimal;

/** Hasil agregasi transaksi (jumlah baris, total pemasukan, total pengeluaran). */
public class Ringkasan {
    private final int jumlah;
    private final BigDecimal pemasukan;
    private final BigDecimal pengeluaran;

    public Ringkasan(int jumlah, BigDecimal pemasukan, BigDecimal pengeluaran) {
        this.jumlah = jumlah;
        this.pemasukan = pemasukan;
        this.pengeluaran = pengeluaran;
    }

    public int getJumlah() { return jumlah; }
    public BigDecimal getPemasukan() { return pemasukan; }
    public BigDecimal getPengeluaran() { return pengeluaran; }
    public BigDecimal getSaldo() { return pemasukan.subtract(pengeluaran); }
}
