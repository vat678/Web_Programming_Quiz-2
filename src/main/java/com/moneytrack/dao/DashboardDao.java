package com.moneytrack.dao;

import com.moneytrack.model.BulanTotal;
import com.moneytrack.model.KategoriTotal;
import com.moneytrack.util.Db;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Query agregasi khusus dashboard. */
public class DashboardDao {
    private static final Locale ID = Locale.forLanguageTag("id-ID");

    /** Total pemasukan/pengeluaran per bulan untuk {@code jumlahBulan} bulan terakhir (termasuk bulan ini). */
    public List<BulanTotal> perBulan(long userId, int jumlahBulan) throws SQLException {
        YearMonth sekarang = YearMonth.now();
        YearMonth awal = sekarang.minusMonths(jumlahBulan - 1L);

        Map<YearMonth, BigDecimal[]> data = new LinkedHashMap<>();
        for (int i = 0; i < jumlahBulan; i++) {
            data.put(awal.plusMonths(i), new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
        }

        String sql = "SELECT YEAR(tanggal) AS th, MONTH(tanggal) AS bl, jenis, SUM(nominal) AS total "
                + "FROM transaksi WHERE user_id = ? AND tanggal >= ? "
                + "GROUP BY YEAR(tanggal), MONTH(tanggal), jenis";
        try (Connection c = Db.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setDate(2, Date.valueOf(awal.atDay(1)));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    BigDecimal[] v = data.get(YearMonth.of(rs.getInt("th"), rs.getInt("bl")));
                    if (v == null) continue;
                    v["pemasukan".equals(rs.getString("jenis")) ? 0 : 1] = rs.getBigDecimal("total");
                }
            }
        }

        BigDecimal maks = BigDecimal.ZERO;
        for (BigDecimal[] v : data.values()) maks = maks.max(v[0]).max(v[1]);

        List<BulanTotal> hasil = new ArrayList<>();
        for (Map.Entry<YearMonth, BigDecimal[]> e : data.entrySet()) {
            String label = e.getKey().getMonth().getDisplayName(TextStyle.SHORT, ID);
            BulanTotal b = new BulanTotal(label, e.getValue()[0], e.getValue()[1]);
            b.setTinggiPemasukan(persen(e.getValue()[0], maks, 3));
            b.setTinggiPengeluaran(persen(e.getValue()[1], maks, 3));
            hasil.add(b);
        }
        return hasil;
    }

    /** Pengeluaran per kategori pada rentang tanggal, urut terbesar, maksimal {@code limit} baris. */
    public List<KategoriTotal> pengeluaranPerKategori(long userId, LocalDate dari, LocalDate sampai, int limit)
            throws SQLException {
        String sql = "SELECT COALESCE(k.nama, 'Tanpa kategori') AS nama, SUM(t.nominal) AS total "
                + "FROM transaksi t LEFT JOIN kategori k ON k.id = t.kategori_id "
                + "WHERE t.user_id = ? AND t.jenis = 'pengeluaran' AND t.tanggal BETWEEN ? AND ? "
                + "GROUP BY COALESCE(k.nama, 'Tanpa kategori') ORDER BY total DESC LIMIT ?";
        List<KategoriTotal> list = new ArrayList<>();
        try (Connection c = Db.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setDate(2, Date.valueOf(dari));
            ps.setDate(3, Date.valueOf(sampai));
            ps.setInt(4, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(new KategoriTotal(rs.getString("nama"), rs.getBigDecimal("total")));
            }
        }
        BigDecimal jumlah = BigDecimal.ZERO;
        for (KategoriTotal k : list) jumlah = jumlah.add(k.getTotal());
        for (KategoriTotal k : list) k.setPersen(persen(k.getTotal(), jumlah, 0));
        return list;
    }

    /** Nilai / pembagi dalam persen (0-100); {@code minimal} dipakai agar batang kecil tetap terlihat. */
    private static int persen(BigDecimal nilai, BigDecimal pembagi, int minimal) {
        if (pembagi.signum() == 0 || nilai.signum() == 0) return 0;
        int p = nilai.multiply(BigDecimal.valueOf(100)).divide(pembagi, 0, java.math.RoundingMode.HALF_UP).intValue();
        return Math.max(p, minimal);
    }
}
