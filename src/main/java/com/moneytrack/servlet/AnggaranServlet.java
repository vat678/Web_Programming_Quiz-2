package com.moneytrack.servlet;

import com.moneytrack.dao.AnggaranDao;
import com.moneytrack.dao.KategoriDao;
import com.moneytrack.model.Anggaran;
import com.moneytrack.model.Kategori;
import com.moneytrack.model.SessionUser;
import com.moneytrack.util.Flash;
import com.moneytrack.util.Form;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Modul Anggaran (CRUD): batas pengeluaran per kategori per bulan.
 * GET /anggaran?bulan=&tahun=, GET /anggaran/edit?id=, POST /anggaran/create, /update, /delete.
 */
@WebServlet("/anggaran/*")
public class AnggaranServlet extends HttpServlet {
    private static final String VIEW = "/WEB-INF/views/anggaran/index.jsp";
    private static final String EDIT = "/WEB-INF/views/anggaran/edit.jsp";
    private static final List<String> NAMA_BULAN = List.of("Januari", "Februari", "Maret", "April", "Mei", "Juni",
            "Juli", "Agustus", "September", "Oktober", "November", "Desember");

    private final AnggaranDao dao = new AnggaranDao();
    private final KategoriDao kategoriDao = new KategoriDao();

    private static long uid(HttpServletRequest req) {
        return ((SessionUser) req.getSession().getAttribute("user")).getId();
    }

    private static String path(HttpServletRequest req) {
        String p = req.getPathInfo();
        return p == null ? "/" : p;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            switch (path(req)) {
                case "/" -> showIndex(req, resp);
                case "/edit" -> {
                    Long id = Form.id(req, "id");
                    Anggaran a = id == null ? null : dao.find(id, uid(req));
                    if (a == null) {
                        resp.sendError(404);
                        return;
                    }
                    showEdit(req, resp, a);
                }
                default -> resp.sendError(404);
            }
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            switch (path(req)) {
                case "/create" -> create(req, resp);
                case "/update" -> update(req, resp);
                case "/delete" -> delete(req, resp);
                default -> resp.sendError(404);
            }
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    // ---------- tampilan ----------

    /** Periode yang ditampilkan: dari parameter bulan/tahun bila valid, selain itu bulan ini. */
    private static int[] periode(HttpServletRequest req, String bulanParam, String tahunParam) {
        LocalDate now = LocalDate.now();
        int bulan = now.getMonthValue();
        int tahun = now.getYear();
        try {
            int b = Integer.parseInt(req.getParameter(bulanParam).trim());
            if (b >= 1 && b <= 12) bulan = b;
        } catch (RuntimeException ignored) {
            // default bulan ini
        }
        try {
            int t = Integer.parseInt(req.getParameter(tahunParam).trim());
            if (t >= 2000 && t <= 2100) tahun = t;
        } catch (RuntimeException ignored) {
            // default tahun ini
        }
        return new int[]{bulan, tahun};
    }

    private void showIndex(HttpServletRequest req, HttpServletResponse resp)
            throws SQLException, ServletException, IOException {
        long userId = uid(req);
        int[] p = periode(req, "bulan", "tahun");
        List<Anggaran> list = dao.findByPeriode(userId, p[0], p[1]);

        BigDecimal total = BigDecimal.ZERO;
        BigDecimal terpakai = BigDecimal.ZERO;
        for (Anggaran a : list) {
            total = total.add(a.getJumlah());
            terpakai = terpakai.add(a.getTerpakai());
        }

        req.setAttribute("anggarans", list);
        req.setAttribute("totalAnggaran", total);
        req.setAttribute("totalTerpakai", terpakai);
        req.setAttribute("totalSisa", total.subtract(terpakai));
        req.setAttribute("bulan", p[0]);
        req.setAttribute("tahun", p[1]);
        req.setAttribute("namaBulan", NAMA_BULAN);
        req.setAttribute("periodeLabel", NAMA_BULAN.get(p[0] - 1) + " " + p[1]);
        req.setAttribute("kategoris", kategoriDao.findAll(userId, "pengeluaran"));
        req.setAttribute("tahunIni", LocalDate.now().getYear());
        req.getRequestDispatcher(VIEW).forward(req, resp);
    }

    private void showEdit(HttpServletRequest req, HttpServletResponse resp, Anggaran a)
            throws SQLException, ServletException, IOException {
        req.setAttribute("anggaran", a);
        req.setAttribute("namaBulan", NAMA_BULAN);
        req.setAttribute("kategoris", kategoriDao.findAll(uid(req), "pengeluaran"));
        req.getRequestDispatcher(EDIT).forward(req, resp);
    }

    // ---------- aksi ----------

    /** Hasil baca form: nilai yang null berarti ada error di Form. */
    private record Input(Long kategoriId, BigDecimal jumlah, Integer bulan, Integer tahun) {}

    private Input read(HttpServletRequest req, Form f, long userId, long excludeId) throws SQLException {
        BigDecimal jumlah = f.money("jumlah_anggaran", "Jumlah anggaran", false);
        Integer bulan = f.integer("bulan", "Bulan", 1, 12);
        Integer tahun = f.integer("tahun", "Tahun", 2000, 2100);

        // Kategori harus milik pengguna ini (anti-IDOR) dan bertipe pengeluaran.
        Long kid = Form.id(req, "kategori_id");
        Kategori k = kid == null ? null : kategoriDao.find(kid, userId);
        if (k == null) {
            f.error("kategori_id", "Kategori yang dipilih tidak valid.");
            kid = null;
        } else if (!"pengeluaran".equals(k.getJenis())) {
            f.error("kategori_id", "Anggaran hanya dapat dibuat untuk kategori pengeluaran.");
            kid = null;
        }

        if (f.ok() && dao.exists(userId, kid, bulan, tahun, excludeId)) {
            f.error("kategori_id", "Anggaran untuk kategori ini pada periode tersebut sudah ada.");
        }
        return new Input(kid, jumlah, bulan, tahun);
    }

    private void create(HttpServletRequest req, HttpServletResponse resp)
            throws SQLException, ServletException, IOException {
        long userId = uid(req);
        Form f = new Form(req);
        Input in = read(req, f, userId, 0);
        if (!f.ok()) {
            f.publish();
            showIndexForForm(req, resp);
            return;
        }
        dao.insert(userId, in.kategoriId(), in.jumlah(), in.bulan(), in.tahun());
        Flash.success(req, "Anggaran berhasil ditambahkan.");
        resp.sendRedirect(req.getContextPath() + "/anggaran?bulan=" + in.bulan() + "&tahun=" + in.tahun());
    }

    /** Saat validasi gagal, tampilkan daftar untuk periode yang diketik di form. */
    private void showIndexForForm(HttpServletRequest req, HttpServletResponse resp)
            throws SQLException, ServletException, IOException {
        // showIndex membaca parameter bulan/tahun yang sama, sehingga periode form ikut tampil.
        showIndex(req, resp);
    }

    private void update(HttpServletRequest req, HttpServletResponse resp)
            throws SQLException, ServletException, IOException {
        long userId = uid(req);
        Long id = Form.id(req, "id");
        Anggaran lama = id == null ? null : dao.find(id, userId);   // cek kepemilikan
        if (lama == null) {
            resp.sendError(404);
            return;
        }
        Form f = new Form(req);
        Input in = read(req, f, userId, id);
        if (!f.ok()) {
            f.publish();
            showEdit(req, resp, lama);
            return;
        }
        dao.update(id, userId, in.kategoriId(), in.jumlah(), in.bulan(), in.tahun());
        Flash.success(req, "Anggaran berhasil diperbarui.");
        resp.sendRedirect(req.getContextPath() + "/anggaran?bulan=" + in.bulan() + "&tahun=" + in.tahun());
    }

    private void delete(HttpServletRequest req, HttpServletResponse resp) throws SQLException, IOException {
        long userId = uid(req);
        Long id = Form.id(req, "id");
        Anggaran a = id == null ? null : dao.find(id, userId);
        if (a == null) {
            resp.sendError(404);
            return;
        }
        dao.delete(id, userId);
        Flash.success(req, "Anggaran berhasil dihapus.");
        resp.sendRedirect(req.getContextPath() + "/anggaran?bulan=" + a.getBulan() + "&tahun=" + a.getTahun());
    }
}
