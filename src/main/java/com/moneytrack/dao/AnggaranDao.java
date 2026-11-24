package com.moneytrack.dao;

import com.moneytrack.model.Anggaran;
import com.moneytrack.util.Db;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AnggaranDao {

    /** terpakai = total pengeluaran pada kategori tersebut di bulan/tahun anggaran (dihitung, tidak disimpan). */
    private static final String SELECT =
            "SELECT a.id, a.user_id, a.kategori_id, a.jumlah_anggaran, a.bulan, a.tahun, k.nama AS kategori_nama, "
          + "COALESCE((SELECT SUM(t.nominal) FROM transaksi t "
          + "          WHERE t.user_id = a.user_id AND t.kategori_id = a.kategori_id AND t.jenis = 'pengeluaran' "
          + "            AND t.tanggal >= MAKEDATE(a.tahun, 1) + INTERVAL (a.bulan - 1) MONTH "
          + "            AND t.tanggal <  MAKEDATE(a.tahun, 1) + INTERVAL a.bulan MONTH), 0) AS terpakai "
          + "FROM anggaran a JOIN kategori k ON k.id = a.kategori_id ";

    private static Anggaran map(ResultSet rs) throws SQLException {
        Anggaran a = new Anggaran();
        a.setId(rs.getLong("id"));
        a.setUserId(rs.getLong("user_id"));
        a.setKategoriId(rs.getLong("kategori_id"));
        a.setKategoriNama(rs.getString("kategori_nama"));
        a.setJumlah(rs.getBigDecimal("jumlah_anggaran"));
        a.setBulan(rs.getInt("bulan"));
        a.setTahun(rs.getInt("tahun"));
        a.setTerpakai(rs.getBigDecimal("terpakai"));
        return a;
    }

    public List<Anggaran> findByPeriode(long userId, int bulan, int tahun) throws SQLException {
        List<Anggaran> list = new ArrayList<>();
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement(
                     SELECT + "WHERE a.user_id = ? AND a.bulan = ? AND a.tahun = ? ORDER BY k.nama")) {
            ps.setLong(1, userId);
            ps.setInt(2, bulan);
            ps.setInt(3, tahun);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    public Anggaran find(long id, long userId) throws SQLException {
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement(SELECT + "WHERE a.id = ? AND a.user_id = ?")) {
            ps.setLong(1, id);
            ps.setLong(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public boolean exists(long userId, long kategoriId, int bulan, int tahun, long excludeId) throws SQLException {
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT 1 FROM anggaran WHERE user_id = ? AND kategori_id = ? AND bulan = ? AND tahun = ? AND id <> ?")) {
            ps.setLong(1, userId);
            ps.setLong(2, kategoriId);
            ps.setInt(3, bulan);
            ps.setInt(4, tahun);
            ps.setLong(5, excludeId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public void insert(long userId, long kategoriId, BigDecimal jumlah, int bulan, int tahun) throws SQLException {
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement(
                     "INSERT INTO anggaran (user_id, kategori_id, jumlah_anggaran, bulan, tahun) VALUES (?,?,?,?,?)")) {
            ps.setLong(1, userId);
            ps.setLong(2, kategoriId);
            ps.setBigDecimal(3, jumlah);
            ps.setInt(4, bulan);
            ps.setInt(5, tahun);
            ps.executeUpdate();
        }
    }

    public boolean update(long id, long userId, long kategoriId, BigDecimal jumlah, int bulan, int tahun)
            throws SQLException {
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement(
                     "UPDATE anggaran SET kategori_id = ?, jumlah_anggaran = ?, bulan = ?, tahun = ? "
                   + "WHERE id = ? AND user_id = ?")) {
            ps.setLong(1, kategoriId);
            ps.setBigDecimal(2, jumlah);
            ps.setInt(3, bulan);
            ps.setInt(4, tahun);
            ps.setLong(5, id);
            ps.setLong(6, userId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(long id, long userId) throws SQLException {
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement("DELETE FROM anggaran WHERE id = ? AND user_id = ?")) {
            ps.setLong(1, id);
            ps.setLong(2, userId);
            return ps.executeUpdate() > 0;
        }
    }
}
