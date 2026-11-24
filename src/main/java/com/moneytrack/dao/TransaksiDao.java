package com.moneytrack.dao;

import com.moneytrack.model.Ringkasan;
import com.moneytrack.model.Transaksi;
import com.moneytrack.model.TransaksiFilter;
import com.moneytrack.util.Db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TransaksiDao {

    private static final String FROM =
            "FROM transaksi t "
          + "LEFT JOIN kategori k ON k.id = t.kategori_id "
          + "JOIN dompet d ON d.id = t.dompet_id ";

    private static Transaksi map(ResultSet rs) throws SQLException {
        Transaksi t = new Transaksi();
        t.setId(rs.getLong("id"));
        t.setUserId(rs.getLong("user_id"));
        t.setDompetId(rs.getLong("dompet_id"));
        long kid = rs.getLong("kategori_id");
        t.setKategoriId(rs.wasNull() ? null : kid);
        t.setJenis(rs.getString("jenis"));
        t.setJudul(rs.getString("judul"));
        t.setNominal(rs.getBigDecimal("nominal"));
        t.setTanggal(rs.getObject("tanggal", LocalDate.class));
        t.setCatatan(rs.getString("catatan"));
        t.setKategoriNama(rs.getString("kategori_nama"));
        t.setDompetNama(rs.getString("dompet_nama"));
        return t;
    }

    private static String escapeLike(String s) {
        return s.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }

    /** Membangun WHERE dinamis; semua nilai lewat parameter (PreparedStatement), bukan digabung ke string. */
    private static String where(long userId, TransaksiFilter f, List<Object> p) {
        StringBuilder sb = new StringBuilder("WHERE t.user_id = ? ");
        p.add(userId);
        if (!f.getQ().isEmpty()) {
            sb.append("AND (t.judul LIKE ? ESCAPE '\\\\' OR t.catatan LIKE ? ESCAPE '\\\\') ");
            String like = "%" + escapeLike(f.getQ()) + "%";
            p.add(like);
            p.add(like);
        }
        if (!f.getJenis().isEmpty()) {
            sb.append("AND t.jenis = ? ");
            p.add(f.getJenis());
        }
        if (f.getKategoriId() != null) {
            sb.append("AND t.kategori_id = ? ");
            p.add(f.getKategoriId());
        }
        if (f.getDompetId() != null) {
            sb.append("AND t.dompet_id = ? ");
            p.add(f.getDompetId());
        }
        if (f.getDari() != null) {
            sb.append("AND t.tanggal >= ? ");
            p.add(f.getDari());
        }
        if (f.getSampai() != null) {
            sb.append("AND t.tanggal <= ? ");
            p.add(f.getSampai());
        }
        return sb.toString();
    }

    private static void bind(PreparedStatement ps, List<Object> params) throws SQLException {
        for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
    }

    public List<Transaksi> findPage(long userId, TransaksiFilter f, int offset, int limit) throws SQLException {
        List<Object> p = new ArrayList<>();
        String sql = "SELECT t.*, k.nama AS kategori_nama, d.nama AS dompet_nama " + FROM
                + where(userId, f, p) + "ORDER BY t.tanggal DESC, t.id DESC LIMIT ? OFFSET ?";
        p.add(limit);
        p.add(offset);
        List<Transaksi> list = new ArrayList<>();
        try (Connection c = Db.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, p);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    /** Jumlah baris dan total pemasukan/pengeluaran untuk filter yang sama (satu query). */
    public Ringkasan ringkasan(long userId, TransaksiFilter f) throws SQLException {
        List<Object> p = new ArrayList<>();
        String sql = "SELECT COUNT(*) AS jml, "
                + "COALESCE(SUM(CASE WHEN t.jenis = 'pemasukan' THEN t.nominal END), 0) AS pm, "
                + "COALESCE(SUM(CASE WHEN t.jenis = 'pengeluaran' THEN t.nominal END), 0) AS pk "
                + FROM + where(userId, f, p);
        try (Connection c = Db.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, p);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return new Ringkasan(rs.getInt("jml"), rs.getBigDecimal("pm"), rs.getBigDecimal("pk"));
            }
        }
    }

    public Transaksi find(long id, long userId) throws SQLException {
        String sql = "SELECT t.*, k.nama AS kategori_nama, d.nama AS dompet_nama " + FROM
                + "WHERE t.id = ? AND t.user_id = ?";
        try (Connection c = Db.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.setLong(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public void insert(Transaksi t) throws SQLException {
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement(
                     "INSERT INTO transaksi (user_id, dompet_id, kategori_id, jenis, judul, nominal, tanggal, catatan) "
                   + "VALUES (?,?,?,?,?,?,?,?)")) {
            fill(ps, t);
            ps.executeUpdate();
        }
    }

    private static void fill(PreparedStatement ps, Transaksi t) throws SQLException {
        ps.setLong(1, t.getUserId());
        ps.setLong(2, t.getDompetId());
        if (t.getKategoriId() == null) ps.setNull(3, Types.BIGINT);
        else ps.setLong(3, t.getKategoriId());
        ps.setString(4, t.getJenis());
        ps.setString(5, t.getJudul());
        ps.setBigDecimal(6, t.getNominal());
        ps.setObject(7, t.getTanggal());
        ps.setString(8, t.getCatatan());
    }

    public boolean update(Transaksi t) throws SQLException {
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement(
                     "UPDATE transaksi SET user_id = ?, dompet_id = ?, kategori_id = ?, jenis = ?, judul = ?, "
                   + "nominal = ?, tanggal = ?, catatan = ? WHERE id = ? AND user_id = ?")) {
            fill(ps, t);
            ps.setLong(9, t.getId());
            ps.setLong(10, t.getUserId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(long id, long userId) throws SQLException {
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement("DELETE FROM transaksi WHERE id = ? AND user_id = ?")) {
            ps.setLong(1, id);
            ps.setLong(2, userId);
            return ps.executeUpdate() > 0;
        }
    }
}
