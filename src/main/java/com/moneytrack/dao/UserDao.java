package com.moneytrack.dao;

import com.moneytrack.model.User;
import com.moneytrack.util.Db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.moneytrack.model.UserAdminRow;
import java.util.ArrayList;
import java.util.List;

public class UserDao {

    private static User map(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getLong("id"));
        u.setNama(rs.getString("nama"));
        u.setEmail(rs.getString("email"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setRole(rs.getString("role"));
        u.setCreatedAt(rs.getObject("created_at", java.time.LocalDateTime.class));
        return u;
    }

    public User findByEmail(String email) throws SQLException {
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM users WHERE email = ?")) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public boolean emailExists(String email) throws SQLException {
        return findByEmail(email) != null;
    }

    public int countByRole(String role) throws SQLException {
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM users WHERE role = ?")) {
            ps.setString(1, role);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    private long insertUser(Connection c, String nama, String email, String hash, String role) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "INSERT INTO users (nama, email, password_hash, role) VALUES (?,?,?,?)",
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, nama);
            ps.setString(2, email);
            ps.setString(3, hash);
            ps.setString(4, role);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                return rs.getLong(1);
            }
        }
    }

    /** Membuat akun admin (dipakai saat start aplikasi jika belum ada admin). */
    public long createAdmin(String nama, String email, String hash) throws SQLException {
        try (Connection c = Db.get()) {
            return insertUser(c, nama, email, hash, "admin");
        }
    }

    public long register(String nama, String email, String hash) throws SQLException {
        try (Connection c = Db.get()) {
            c.setAutoCommit(false);
            try {
                long uid = insertUser(c, nama, email, hash, "user");
                try (PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO dompet (user_id, nama, tipe, saldo_awal) VALUES (?, 'Dompet Tunai', 'tunai', 0)")) {
                    ps.setLong(1, uid);
                    ps.executeUpdate();
                }
                String[][] defaults = {
                        {"Gaji", "pemasukan"}, {"Makanan", "pengeluaran"}, {"Transportasi", "pengeluaran"},
                        {"Tagihan", "pengeluaran"}, {"Hiburan", "pengeluaran"}};
                try (PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO kategori (user_id, nama, jenis) VALUES (?,?,?)")) {
                    for (String[] d : defaults) {
                        ps.setLong(1, uid);
                        ps.setString(2, d[0]);
                        ps.setString(3, d[1]);
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
                c.commit();
                return uid;
            } catch (SQLException e) {
                c.rollback();
                throw e;
            }
        }
    }

    public User findById(long id) throws SQLException {
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM users WHERE id = ?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public boolean emailDipakaiLain(String email, long excludeId) throws SQLException {
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement("SELECT 1 FROM users WHERE email = ? AND id <> ?")) {
            ps.setString(1, email);
            ps.setLong(2, excludeId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean updateProfil(long id, String nama, String email) throws SQLException {
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement("UPDATE users SET nama = ?, email = ? WHERE id = ?")) {
            ps.setString(1, nama);
            ps.setString(2, email);
            ps.setLong(3, id);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean updatePassword(long id, String hash) throws SQLException {
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement("UPDATE users SET password_hash = ? WHERE id = ?")) {
            ps.setString(1, hash);
            ps.setLong(2, id);
            return ps.executeUpdate() > 0;
        }
    }

    public long[] statistik(long userId) throws SQLException {
        String sql = "SELECT (SELECT COUNT(*) FROM dompet WHERE user_id = ?), "
                   + "(SELECT COUNT(*) FROM kategori WHERE user_id = ?), "
                   + "(SELECT COUNT(*) FROM transaksi WHERE user_id = ?)";
        try (Connection c = Db.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 1; i <= 3; i++) ps.setLong(i, userId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return new long[]{rs.getLong(1), rs.getLong(2), rs.getLong(3)};
            }
        }
    }

    public List<UserAdminRow> cari(String q, String role) throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT u.id, u.nama, u.email, u.role, u.created_at, "
              + "(SELECT COUNT(*) FROM dompet d WHERE d.user_id = u.id) AS jml_dompet, "
              + "(SELECT COUNT(*) FROM transaksi t WHERE t.user_id = u.id) AS jml_transaksi "
              + "FROM users u WHERE 1=1");
        List<String> params = new ArrayList<>();
        if (q != null && !q.isBlank()) {
            String like = "%" + q.trim().replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_") + "%";
            sql.append(" AND (u.nama LIKE ? OR u.email LIKE ?)");
            params.add(like);
            params.add(like);
        }
        if (role != null && !role.isBlank()) {
            sql.append(" AND u.role = ?");
            params.add(role);
        }
        sql.append(" ORDER BY u.created_at DESC, u.id DESC");

        List<UserAdminRow> out = new ArrayList<>();
        try (Connection c = Db.get(); PreparedStatement ps = c.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setString(i + 1, params.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    UserAdminRow r = new UserAdminRow();
                    r.setId(rs.getLong("id"));
                    r.setNama(rs.getString("nama"));
                    r.setEmail(rs.getString("email"));
                    r.setRole(rs.getString("role"));
                    r.setCreatedAt(rs.getObject("created_at", java.time.LocalDateTime.class));
                    r.setJumlahDompet(rs.getLong("jml_dompet"));
                    r.setJumlahTransaksi(rs.getLong("jml_transaksi"));
                    out.add(r);
                }
            }
        }
        return out;
    }

    public boolean updateOlehAdmin(long id, String nama, String email, String role) throws SQLException {
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement("UPDATE users SET nama = ?, email = ?, role = ? WHERE id = ?")) {
            ps.setString(1, nama);
            ps.setString(2, email);
            ps.setString(3, role);
            ps.setLong(4, id);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean hapusBeserta(long id) throws SQLException {
        String[] anak = {
                "DELETE FROM transaksi_berulang WHERE user_id = ?",
                "DELETE FROM transaksi WHERE user_id = ?",
                "DELETE FROM anggaran WHERE user_id = ?",
                "DELETE FROM target_tabungan WHERE user_id = ?",
                "DELETE FROM hutang_piutang WHERE user_id = ?",
                "DELETE FROM kategori WHERE user_id = ?",
                "DELETE FROM dompet WHERE user_id = ?"
        };
        try (Connection c = Db.get()) {
            c.setAutoCommit(false);
            try {
                for (String sql : anak) {
                    try (PreparedStatement ps = c.prepareStatement(sql)) {
                        ps.setLong(1, id);
                        ps.executeUpdate();
                    }
                }
                int n;
                try (PreparedStatement ps = c.prepareStatement("DELETE FROM users WHERE id = ?")) {
                    ps.setLong(1, id);
                    n = ps.executeUpdate();
                }
                c.commit();
                return n > 0;
            } catch (SQLException e) {
                c.rollback();
                throw e;
            }
        }
    }
}
