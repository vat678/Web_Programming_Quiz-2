package com.moneytrack.model;

import java.math.BigDecimal;

public class Dompet {
    private long id;
    private long userId;
    private String nama;
    private String tipe;
    private BigDecimal saldoAwal = BigDecimal.ZERO;
    private BigDecimal saldo = BigDecimal.ZERO;   // saldo_awal + pemasukan - pengeluaran (dihitung di query)
    private int jumlahTransaksi;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }
    public String getNama() { return nama; }
    public void setNama(String nama) { this.nama = nama; }
    public String getTipe() { return tipe; }
    public void setTipe(String tipe) { this.tipe = tipe; }
    public BigDecimal getSaldoAwal() { return saldoAwal; }
    public void setSaldoAwal(BigDecimal saldoAwal) { this.saldoAwal = saldoAwal; }
    public BigDecimal getSaldo() { return saldo; }
    public void setSaldo(BigDecimal saldo) { this.saldo = saldo; }
    public int getJumlahTransaksi() { return jumlahTransaksi; }
    public void setJumlahTransaksi(int jumlahTransaksi) { this.jumlahTransaksi = jumlahTransaksi; }

    public String getTipeLabel() {
        if ("bank".equals(tipe)) return "Bank";
        if ("ewallet".equals(tipe)) return "E-Wallet";
        return "Tunai";
    }
}
