package com.moneytrack.dao;

import com.moneytrack.model.TargetTabungan;
import com.moneytrack.util.Db;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TargetTabunganDao {

    private static TargetTabungan map(ResultSet rs) throws SQLException {
        TargetTabungan t = new TargetTabungan();
        t.setId(rs.getLong("id"));
        t.setUserId(rs.getLong("user_id"));
        t.setNama(rs.getString("nama"));
        t.setTargetNominal(rs.getBigDecimal("target_nominal"));
        t.setTerkumpul(rs.getBigDecimal("terkumpul"));
        t.setTenggat(rs.getObject("tenggat", LocalDate.class));
        t.setCatatan(rs.getString("catatan"));
        return t;
    }

    /** Target yang belum tercapai tampil lebih dulu, diurutkan dari tenggat terdekat. */
    public List<TargetTabungan> findAll(long userId) throws SQLException {
        List<TargetTabungan> list = new ArrayList<>();
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT * FROM target_tabungan WHERE user_id = ? "
                   + "ORDER BY (terkumpul >= target_nominal), tenggat IS NULL, tenggat, id DESC")) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    public TargetTabungan find(long id, long userId) throws SQLException {
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM target_tabungan WHERE id = ? AND user_id = ?")) {
            ps.setLong(1, id);
            ps.setLong(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public void insert(TargetTabungan t) throws SQLException {
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement(
                     "INSERT INTO target_tabungan (user_id, nama, target_nominal, terkumpul, tenggat, catatan) "
                   + "VALUES (?,?,?,?,?,?)")) {
            ps.setLong(1, t.getUserId());
            fill(ps, t, 2);
            ps.executeUpdate();
        }
    }

    public boolean update(TargetTabungan t) throws SQLException {
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement(
                     "UPDATE target_tabungan SET nama = ?, target_nominal = ?, terkumpul = ?, tenggat = ?, catatan = ? "
                   + "WHERE id = ? AND user_id = ?")) {
            fill(ps, t, 1);
            ps.setLong(6, t.getId());
            ps.setLong(7, t.getUserId());
            return ps.executeUpdate() > 0;
        }
    }

    /** Mengisi 5 kolom: nama, target_nominal, terkumpul, tenggat, catatan mulai dari indeks {@code start}. */
    private static void fill(PreparedStatement ps, TargetTabungan t, int start) throws SQLException {
        ps.setString(start, t.getNama());
        ps.setBigDecimal(start + 1, t.getTargetNominal());
        ps.setBigDecimal(start + 2, t.getTerkumpul());
        if (t.getTenggat() == null) ps.setNull(start + 3, Types.DATE);
        else ps.setObject(start + 3, t.getTenggat());
        ps.setString(start + 4, t.getCatatan());
    }

    /** Menambah jumlah terkumpul secara atomik (tidak menimpa nilai dari request lain). */
    public boolean tambahSetoran(long id, long userId, BigDecimal nominal) throws SQLException {
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement(
                     "UPDATE target_tabungan SET terkumpul = terkumpul + ? WHERE id = ? AND user_id = ?")) {
            ps.setBigDecimal(1, nominal);
            ps.setLong(2, id);
            ps.setLong(3, userId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(long id, long userId) throws SQLException {
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement("DELETE FROM target_tabungan WHERE id = ? AND user_id = ?")) {
            ps.setLong(1, id);
            ps.setLong(2, userId);
            return ps.executeUpdate() > 0;
        }
    }
}
