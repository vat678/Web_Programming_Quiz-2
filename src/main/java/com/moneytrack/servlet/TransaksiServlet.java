package com.moneytrack.servlet;

import com.moneytrack.dao.DompetDao;
import com.moneytrack.dao.KategoriDao;
import com.moneytrack.dao.TransaksiDao;
import com.moneytrack.model.*;
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
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Set;

/**
 * Modul Transaksi (CRUD + pencarian, filter, pagination).
 * GET /transaksi?q=&jenis=&kategori=&dompet=&dari=&sampai=&page=, GET /transaksi/edit?id=,
 * POST /transaksi/create, /update, /delete.
 */
@WebServlet("/transaksi/*")
public class TransaksiServlet extends HttpServlet {
    private static final String VIEW = "/WEB-INF/views/transaksi/index.jsp";
    private static final String EDIT = "/WEB-INF/views/transaksi/edit.jsp";
    private static final int PAGE_SIZE = 10;
    private static final Set<String> JENIS = Set.of("pemasukan", "pengeluaran");

    private final TransaksiDao dao = new TransaksiDao();
    private final KategoriDao kategoriDao = new KategoriDao();
    private final DompetDao dompetDao = new DompetDao();

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
                    Transaksi t = id == null ? null : dao.find(id, uid(req));
                    if (t == null) {
                        resp.sendError(404);
                        return;
                    }
                    showEdit(req, resp, t);
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

    private static TransaksiFilter parseFilter(HttpServletRequest req) {
        TransaksiFilter f = new TransaksiFilter();
        String q = req.getParameter("q");
        f.setQ(q == null ? "" : q.trim());
        String jenis = req.getParameter("jenis");
        f.setJenis(jenis != null && JENIS.contains(jenis) ? jenis : "");
        f.setKategoriId(Form.id(req, "kategori"));
        f.setDompetId(Form.id(req, "dompet"));
        f.setDari(parseDate(req.getParameter("dari")));
        f.setSampai(parseDate(req.getParameter("sampai")));
        return f;
    }

    private static LocalDate parseDate(String s) {
        if (s == null || s.isBlank()) return null;
        try {
            return LocalDate.parse(s.trim());
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private void showIndex(HttpServletRequest req, HttpServletResponse resp)
            throws SQLException, ServletException, IOException {
        long userId = uid(req);
        TransaksiFilter f = parseFilter(req);
        Ringkasan r = dao.ringkasan(userId, f);

        int totalPages = Math.max(1, (int) Math.ceil(r.getJumlah() / (double) PAGE_SIZE));
        int page = 1;
        try {
            page = Integer.parseInt(req.getParameter("page"));
        } catch (NumberFormatException ignored) {
            // default halaman 1
        }
        page = Math.min(Math.max(page, 1), totalPages);

        req.setAttribute("filter", f);
        req.setAttribute("ringkasan", r);
        req.setAttribute("transaksis", dao.findPage(userId, f, (page - 1) * PAGE_SIZE, PAGE_SIZE));
        req.setAttribute("page", page);
        req.setAttribute("totalPages", totalPages);
        req.setAttribute("kategoris", kategoriDao.findAll(userId));
        req.setAttribute("dompets", dompetDao.findAll(userId));
        req.setAttribute("today", LocalDate.now().toString());
        req.getRequestDispatcher(VIEW).forward(req, resp);
    }

    private void showEdit(HttpServletRequest req, HttpServletResponse resp, Transaksi t)
            throws SQLException, ServletException, IOException {
        long userId = uid(req);
        req.setAttribute("transaksi", t);
        req.setAttribute("kategoris", kategoriDao.findAll(userId));
        req.setAttribute("dompets", dompetDao.findAll(userId));
        req.getRequestDispatcher(EDIT).forward(req, resp);
    }

    // ---------- aksi ----------

    /** Membaca + memvalidasi input; mengembalikan objek Transaksi (nilai bisa null jika ada error di Form). */
    private Transaksi read(HttpServletRequest req, Form f, long userId) throws SQLException {
        Transaksi t = new Transaksi();
        t.setUserId(userId);
        t.setJenis(f.oneOf("jenis", "Jenis transaksi", "pemasukan", "pengeluaran"));
        t.setJudul(f.required("judul", "Nama transaksi", 255));
        BigDecimal nominal = f.money("nominal", "Nominal", false);
        t.setNominal(nominal);
        t.setTanggal(f.date("tanggal", "Tanggal transaksi", true));
        t.setCatatan(f.optional("catatan", "Catatan", 5000));

        // Dompet harus milik pengguna ini (anti-IDOR).
        Long dompetId = Form.id(req, "dompet_id");
        if (dompetId == null || dompetDao.find(dompetId, userId) == null) {
            f.error("dompet_id", "Dompet yang dipilih tidak valid.");
        } else {
            t.setDompetId(dompetId);
        }

        // Kategori opsional; bila diisi harus milik pengguna dan jenisnya cocok dengan jenis transaksi.
        String kategoriRaw = f.str("kategori_id");
        if (!kategoriRaw.isEmpty()) {
            Long kid = Form.id(req, "kategori_id");
            Kategori k = kid == null ? null : kategoriDao.find(kid, userId);
            if (k == null) {
                f.error("kategori_id", "Kategori yang dipilih tidak valid.");
            } else if (t.getJenis() != null && !k.getJenis().equals(t.getJenis())) {
                f.error("kategori_id", "Kategori harus sesuai dengan jenis transaksi.");
            } else {
                t.setKategoriId(kid);
            }
        }
        return t;
    }

    private void create(HttpServletRequest req, HttpServletResponse resp)
            throws SQLException, ServletException, IOException {
        long userId = uid(req);
        Form f = new Form(req);
        Transaksi t = read(req, f, userId);
        if (!f.ok()) {
            f.publish();
            showIndex(req, resp);
            return;
        }
        dao.insert(t);
        Flash.success(req, "Transaksi berhasil ditambahkan.");
        resp.sendRedirect(req.getContextPath() + "/transaksi");
    }

    private void update(HttpServletRequest req, HttpServletResponse resp)
            throws SQLException, ServletException, IOException {
        long userId = uid(req);
        Long id = Form.id(req, "id");
        Transaksi lama = id == null ? null : dao.find(id, userId);   // cek kepemilikan
        if (lama == null) {
            resp.sendError(404);
            return;
        }
        Form f = new Form(req);
        Transaksi t = read(req, f, userId);
        if (!f.ok()) {
            f.publish();
            showEdit(req, resp, lama);
            return;
        }
        t.setId(id);
        dao.update(t);
        Flash.success(req, "Transaksi berhasil diperbarui.");
        resp.sendRedirect(req.getContextPath() + "/transaksi");
    }

    private void delete(HttpServletRequest req, HttpServletResponse resp) throws SQLException, IOException {
        Long id = Form.id(req, "id");
        if (id == null || !dao.delete(id, uid(req))) {
            resp.sendError(404);
            return;
        }
        Flash.success(req, "Transaksi berhasil dihapus.");
        resp.sendRedirect(req.getContextPath() + "/transaksi");
    }
}
