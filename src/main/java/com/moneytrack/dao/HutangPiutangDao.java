package com.moneytrack.dao;

import com.moneytrack.model.HutangPiutang;
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

public class HutangPiutangDao {

    private static HutangPiutang map(ResultSet rs) throws SQLException {
        HutangPiutang h = new HutangPiutang();
        h.setId(rs.getLong("id"));
        h.setUserId(rs.getLong("user_id"));
        h.setJenis(rs.getString("jenis"));
        h.setNamaPihak(rs.getString("nama_pihak"));
        h.setNominal(rs.getBigDecimal("nominal"));
        h.setTerbayar(rs.getBigDecimal("terbayar"));
        h.setJatuhTempo(rs.getObject("jatuh_tempo", LocalDate.class));
        h.setCatatan(rs.getString("catatan"));
        return h;
    }

    /** Yang belum lunas tampil lebih dulu, diurutkan dari jatuh tempo terdekat. */
    public List<HutangPiutang> findAll(long userId) throws SQLException {
        List<HutangPiutang> list = new ArrayList<>();
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT * FROM hutang_piutang WHERE user_id = ? "
                   + "ORDER BY (terbayar >= nominal), jatuh_tempo IS NULL, jatuh_tempo, id DESC")) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    public HutangPiutang find(long id, long userId) throws SQLException {
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM hutang_piutang WHERE id = ? AND user_id = ?")) {
            ps.setLong(1, id);
            ps.setLong(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public void insert(HutangPiutang h) throws SQLException {
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement(
                     "INSERT INTO hutang_piutang (jenis, nama_pihak, nominal, terbayar, jatuh_tempo, catatan, user_id) "
                   + "VALUES (?,?,?,?,?,?,?)")) {
            fill(ps, h);
            ps.setLong(7, h.getUserId());
            ps.executeUpdate();
        }
    }

    public boolean update(HutangPiutang h) throws SQLException {
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement(
                     "UPDATE hutang_piutang SET jenis = ?, nama_pihak = ?, nominal = ?, terbayar = ?, "
                   + "jatuh_tempo = ?, catatan = ? WHERE id = ? AND user_id = ?")) {
            fill(ps, h);
            ps.setLong(7, h.getId());
            ps.setLong(8, h.getUserId());
            return ps.executeUpdate() > 0;
        }
    }

    private static void fill(PreparedStatement ps, HutangPiutang h) throws SQLException {
        ps.setString(1, h.getJenis());
        ps.setString(2, h.getNamaPihak());
        ps.setBigDecimal(3, h.getNominal());
        ps.setBigDecimal(4, h.getTerbayar());
        if (h.getJatuhTempo() == null) ps.setNull(5, Types.DATE);
        else ps.setObject(5, h.getJatuhTempo());
        ps.setString(6, h.getCatatan());
    }

    /**
     * Menambah jumlah terbayar secara atomik. Hanya berhasil bila hasilnya tidak melebihi nominal,
     * sehingga dua pembayaran bersamaan tidak bisa membuat terbayar lebih besar dari nominal.
     */
    public boolean bayar(long id, long userId, BigDecimal jumlah) throws SQLException {
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement(
                     "UPDATE hutang_piutang SET terbayar = terbayar + ? "
                   + "WHERE id = ? AND user_id = ? AND terbayar + ? <= nominal")) {
            ps.setBigDecimal(1, jumlah);
            ps.setLong(2, id);
            ps.setLong(3, userId);
            ps.setBigDecimal(4, jumlah);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(long id, long userId) throws SQLException {
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement("DELETE FROM hutang_piutang WHERE id = ? AND user_id = ?")) {
            ps.setLong(1, id);
            ps.setLong(2, userId);
            return ps.executeUpdate() > 0;
        }
    }
}
