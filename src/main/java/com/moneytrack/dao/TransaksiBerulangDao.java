package com.moneytrack.dao;

import com.moneytrack.model.TransaksiBerulang;
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

public class TransaksiBerulangDao {
    /** Batas pengaman: maksimal transaksi yang dibuat per template dalam satu kali proses. */
    private static final int MAKS_PER_TEMPLATE = 366;

    private static final String SELECT =
            "SELECT b.*, d.nama AS dompet_nama, k.nama AS kategori_nama FROM transaksi_berulang b "
          + "JOIN dompet d ON d.id = b.dompet_id LEFT JOIN kategori k ON k.id = b.kategori_id ";

    private static TransaksiBerulang map(ResultSet rs) throws SQLException {
        TransaksiBerulang b = new TransaksiBerulang();
        b.setId(rs.getLong("id"));
        b.setUserId(rs.getLong("user_id"));
        b.setDompetId(rs.getLong("dompet_id"));
        long kid = rs.getLong("kategori_id");
        b.setKategoriId(rs.wasNull() ? null : kid);
        b.setJenis(rs.getString("jenis"));
        b.setJudul(rs.getString("judul"));
        b.setNominal(rs.getBigDecimal("nominal"));
        b.setFrekuensi(rs.getString("frekuensi"));
        b.setTanggalBerikutnya(rs.getObject("tanggal_berikutnya", LocalDate.class));
        b.setAktif(rs.getBoolean("aktif"));
        b.setDompetNama(rs.getString("dompet_nama"));
        b.setKategoriNama(rs.getString("kategori_nama"));
        return b;
    }

    /** Yang aktif lebih dulu, diurutkan dari tanggal terdekat. */
    public List<TransaksiBerulang> findAll(long userId) throws SQLException {
        List<TransaksiBerulang> list = new ArrayList<>();
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement(
                     SELECT + "WHERE b.user_id = ? ORDER BY b.aktif DESC, b.tanggal_berikutnya, b.id")) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    public TransaksiBerulang find(long id, long userId) throws SQLException {
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement(SELECT + "WHERE b.id = ? AND b.user_id = ?")) {
            ps.setLong(1, id);
            ps.setLong(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public void insert(TransaksiBerulang b) throws SQLException {
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement(
                     "INSERT INTO transaksi_berulang (dompet_id, kategori_id, jenis, judul, nominal, frekuensi, "
                   + "tanggal_berikutnya, aktif, user_id) VALUES (?,?,?,?,?,?,?,?,?)")) {
            fill(ps, b);
            ps.setLong(9, b.getUserId());
            ps.executeUpdate();
        }
    }

    public boolean update(TransaksiBerulang b) throws SQLException {
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement(
                     "UPDATE transaksi_berulang SET dompet_id = ?, kategori_id = ?, jenis = ?, judul = ?, nominal = ?, "
                   + "frekuensi = ?, tanggal_berikutnya = ?, aktif = ? WHERE id = ? AND user_id = ?")) {
            fill(ps, b);
            ps.setLong(9, b.getId());
            ps.setLong(10, b.getUserId());
            return ps.executeUpdate() > 0;
        }
    }

    private static void fill(PreparedStatement ps, TransaksiBerulang b) throws SQLException {
        ps.setLong(1, b.getDompetId());
        if (b.getKategoriId() == null) ps.setNull(2, Types.BIGINT);
        else ps.setLong(2, b.getKategoriId());
        ps.setString(3, b.getJenis());
        ps.setString(4, b.getJudul());
        ps.setBigDecimal(5, b.getNominal());
        ps.setString(6, b.getFrekuensi());
        ps.setObject(7, b.getTanggalBerikutnya());
        ps.setBoolean(8, b.isAktif());
    }

    public boolean setAktif(long id, long userId, boolean aktif) throws SQLException {
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement(
                     "UPDATE transaksi_berulang SET aktif = ? WHERE id = ? AND user_id = ?")) {
            ps.setBoolean(1, aktif);
            ps.setLong(2, id);
            ps.setLong(3, userId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(long id, long userId) throws SQLException {
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement("DELETE FROM transaksi_berulang WHERE id = ? AND user_id = ?")) {
            ps.setLong(1, id);
            ps.setLong(2, userId);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Mengubah semua template aktif yang sudah jatuh tempo menjadi transaksi sungguhan.
     * Bila beberapa periode terlewat, semuanya dibuat (masing-masing dengan tanggalnya sendiri), lalu
     * tanggal berikutnya dimajukan. Semua dalam satu transaksi database. Mengembalikan jumlah transaksi dibuat.
     */
    public int prosesJatuhTempo(long userId, LocalDate hariIni) throws SQLException {
        int dibuat = 0;
        try (Connection c = Db.get()) {
            c.setAutoCommit(false);
            try {
                List<TransaksiBerulang> due = new ArrayList<>();
                try (PreparedStatement ps = c.prepareStatement(
                        "SELECT b.*, NULL AS dompet_nama, NULL AS kategori_nama FROM transaksi_berulang b "
                      + "WHERE b.user_id = ? AND b.aktif = 1 AND b.tanggal_berikutnya <= ? FOR UPDATE")) {
                    ps.setLong(1, userId);
                    ps.setObject(2, hariIni);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) due.add(map(rs));
                    }
                }

                try (PreparedStatement ins = c.prepareStatement(
                             "INSERT INTO transaksi (user_id, dompet_id, kategori_id, jenis, judul, nominal, tanggal, catatan) "
                           + "VALUES (?,?,?,?,?,?,?,?)");
                     PreparedStatement upd = c.prepareStatement(
                             "UPDATE transaksi_berulang SET tanggal_berikutnya = ? WHERE id = ?")) {
                    for (TransaksiBerulang b : due) {
                        LocalDate tgl = b.getTanggalBerikutnya();
                        int n = 0;
                        while (!tgl.isAfter(hariIni) && n < MAKS_PER_TEMPLATE) {
                            ins.setLong(1, userId);
                            ins.setLong(2, b.getDompetId());
                            if (b.getKategoriId() == null) ins.setNull(3, Types.BIGINT);
                            else ins.setLong(3, b.getKategoriId());
                            ins.setString(4, b.getJenis());
                            ins.setString(5, b.getJudul());
                            ins.setBigDecimal(6, b.getNominal());
                            ins.setObject(7, tgl);
                            ins.setString(8, "Dibuat otomatis dari transaksi berulang (" + b.getFrekuensi() + ")");
                            ins.addBatch();
                            tgl = TransaksiBerulang.berikutnya(tgl, b.getFrekuensi());
                            n++;
                            dibuat++;
                        }
                        upd.setObject(1, tgl);
                        upd.setLong(2, b.getId());
                        upd.addBatch();
                    }
                    ins.executeBatch();
                    upd.executeBatch();
                }
                c.commit();
            } catch (SQLException | RuntimeException e) {
                c.rollback();
                throw e;
            }
        }
        return dibuat;
    }
}
