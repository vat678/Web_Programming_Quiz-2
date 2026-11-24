package com.moneytrack.dao;

import com.moneytrack.model.Dompet;
import com.moneytrack.util.Db;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DompetDao {

    /** Saldo dihitung dari saldo_awal + total pemasukan - total pengeluaran pada dompet tersebut. */
    private static final String SELECT =
            "SELECT d.id, d.user_id, d.nama, d.tipe, d.saldo_awal, "
          + "d.saldo_awal + COALESCE((SELECT SUM(CASE WHEN t.jenis = 'pemasukan' THEN t.nominal ELSE -t.nominal END) "
          + "                         FROM transaksi t WHERE t.dompet_id = d.id), 0) AS saldo, "
          + "(SELECT COUNT(*) FROM transaksi t WHERE t.dompet_id = d.id) AS jml "
          + "FROM dompet d ";

    private static Dompet map(ResultSet rs) throws SQLException {
        Dompet d = new Dompet();
        d.setId(rs.getLong("id"));
        d.setUserId(rs.getLong("user_id"));
        d.setNama(rs.getString("nama"));
        d.setTipe(rs.getString("tipe"));
        d.setSaldoAwal(rs.getBigDecimal("saldo_awal"));
        BigDecimal saldo = rs.getBigDecimal("saldo");
        d.setSaldo(saldo == null ? BigDecimal.ZERO : saldo);
        d.setJumlahTransaksi(rs.getInt("jml"));
        return d;
    }

    public List<Dompet> findAll(long userId) throws SQLException {
        List<Dompet> list = new ArrayList<>();
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement(SELECT + "WHERE d.user_id = ? ORDER BY d.nama")) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    public Dompet find(long id, long userId) throws SQLException {
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement(SELECT + "WHERE d.id = ? AND d.user_id = ?")) {
            ps.setLong(1, id);
            ps.setLong(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public int countByUser(long userId) throws SQLException {
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM dompet WHERE user_id = ?")) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public boolean namaExists(long userId, String nama, long excludeId) throws SQLException {
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT 1 FROM dompet WHERE user_id = ? AND LOWER(nama) = LOWER(?) AND id <> ?")) {
            ps.setLong(1, userId);
            ps.setString(2, nama);
            ps.setLong(3, excludeId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public void insert(long userId, String nama, String tipe, BigDecimal saldoAwal) throws SQLException {
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement(
                     "INSERT INTO dompet (user_id, nama, tipe, saldo_awal) VALUES (?,?,?,?)")) {
            ps.setLong(1, userId);
            ps.setString(2, nama);
            ps.setString(3, tipe);
            ps.setBigDecimal(4, saldoAwal);
            ps.executeUpdate();
        }
    }

    public boolean update(long id, long userId, String nama, String tipe, BigDecimal saldoAwal) throws SQLException {
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement(
                     "UPDATE dompet SET nama = ?, tipe = ?, saldo_awal = ? WHERE id = ? AND user_id = ?")) {
            ps.setString(1, nama);
            ps.setString(2, tipe);
            ps.setBigDecimal(3, saldoAwal);
            ps.setLong(4, id);
            ps.setLong(5, userId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(long id, long userId) throws SQLException {
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement("DELETE FROM dompet WHERE id = ? AND user_id = ?")) {
            ps.setLong(1, id);
            ps.setLong(2, userId);
            return ps.executeUpdate() > 0;
        }
    }
}
