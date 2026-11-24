package com.moneytrack.dao;

import com.moneytrack.model.Kategori;
import com.moneytrack.util.Db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Semua method menerima userId dan memfilter WHERE user_id = ? (pengganti global scope Laravel). */
public class KategoriDao {

    private static final String SELECT =
            "SELECT k.id, k.user_id, k.nama, k.jenis, "
          + "(EXISTS (SELECT 1 FROM transaksi t WHERE t.kategori_id = k.id) "
          + " OR EXISTS (SELECT 1 FROM anggaran a WHERE a.kategori_id = k.id)) AS dipakai "
          + "FROM kategori k ";

    private static Kategori map(ResultSet rs) throws SQLException {
        Kategori k = new Kategori();
        k.setId(rs.getLong("id"));
        k.setUserId(rs.getLong("user_id"));
        k.setNama(rs.getString("nama"));
        k.setJenis(rs.getString("jenis"));
        k.setDipakai(rs.getBoolean("dipakai"));
        return k;
    }

    public List<Kategori> findAll(long userId) throws SQLException {
        return findAll(userId, null);
    }

    /** jenis boleh null (semua jenis). */
    public List<Kategori> findAll(long userId, String jenis) throws SQLException {
        String sql = SELECT + "WHERE k.user_id = ? " + (jenis != null ? "AND k.jenis = ? " : "")
                + "ORDER BY k.jenis, k.nama";
        List<Kategori> list = new ArrayList<>();
        try (Connection c = Db.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, userId);
            if (jenis != null) ps.setString(2, jenis);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    public Kategori find(long id, long userId) throws SQLException {
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement(SELECT + "WHERE k.id = ? AND k.user_id = ?")) {
            ps.setLong(1, id);
            ps.setLong(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public boolean namaExists(long userId, String nama, String jenis, long excludeId) throws SQLException {
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT 1 FROM kategori WHERE user_id = ? AND jenis = ? AND LOWER(nama) = LOWER(?) AND id <> ?")) {
            ps.setLong(1, userId);
            ps.setString(2, jenis);
            ps.setString(3, nama);
            ps.setLong(4, excludeId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public void insert(long userId, String nama, String jenis) throws SQLException {
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement("INSERT INTO kategori (user_id, nama, jenis) VALUES (?,?,?)")) {
            ps.setLong(1, userId);
            ps.setString(2, nama);
            ps.setString(3, jenis);
            ps.executeUpdate();
        }
    }

    public boolean update(long id, long userId, String nama, String jenis) throws SQLException {
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement(
                     "UPDATE kategori SET nama = ?, jenis = ? WHERE id = ? AND user_id = ?")) {
            ps.setString(1, nama);
            ps.setString(2, jenis);
            ps.setLong(3, id);
            ps.setLong(4, userId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(long id, long userId) throws SQLException {
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement("DELETE FROM kategori WHERE id = ? AND user_id = ?")) {
            ps.setLong(1, id);
            ps.setLong(2, userId);
            return ps.executeUpdate() > 0;
        }
    }
}
