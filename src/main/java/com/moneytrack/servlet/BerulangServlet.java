package com.moneytrack.servlet;

import com.moneytrack.dao.DompetDao;
import com.moneytrack.dao.KategoriDao;
import com.moneytrack.dao.TransaksiBerulangDao;
import com.moneytrack.model.Kategori;
import com.moneytrack.model.SessionUser;
import com.moneytrack.model.TransaksiBerulang;
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
 * Modul Transaksi Berulang (CRUD + jeda/aktifkan + proses jatuh tempo).
 * GET /berulang, GET /berulang/edit?id=, POST /berulang/create, /update, /toggle, /proses, /delete.
 */
@WebServlet("/berulang/*")
public class BerulangServlet extends HttpServlet {
    private static final String VIEW = "/WEB-INF/views/berulang/index.jsp";
    private static final String EDIT = "/WEB-INF/views/berulang/edit.jsp";

    private final TransaksiBerulangDao dao = new TransaksiBerulangDao();
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
                    TransaksiBerulang b = id == null ? null : dao.find(id, uid(req));
                    if (b == null) {
                        resp.sendError(404);
                        return;
                    }
                    showEdit(req, resp, b);
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
                case "/toggle" -> toggle(req, resp);
                case "/proses" -> proses(req, resp);
                case "/delete" -> delete(req, resp);
                default -> resp.sendError(404);
            }
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    private void showIndex(HttpServletRequest req, HttpServletResponse resp)
            throws SQLException, ServletException, IOException {
        long userId = uid(req);
        List<TransaksiBerulang> list = dao.findAll(userId);
        int jatuhTempo = 0;
        int aktif = 0;
        for (TransaksiBerulang b : list) {
            if (b.isAktif()) aktif++;
            if (b.isJatuhTempo()) jatuhTempo++;
        }
        req.setAttribute("items", list);
        req.setAttribute("jumlahAktif", aktif);
        req.setAttribute("jumlahJatuhTempo", jatuhTempo);
        req.setAttribute("kategoris", kategoriDao.findAll(userId));
        req.setAttribute("dompets", dompetDao.findAll(userId));
        req.setAttribute("today", LocalDate.now().toString());
        req.getRequestDispatcher(VIEW).forward(req, resp);
    }

    private void showEdit(HttpServletRequest req, HttpServletResponse resp, TransaksiBerulang b)
            throws SQLException, ServletException, IOException {
        long userId = uid(req);
        req.setAttribute("item", b);
        req.setAttribute("kategoris", kategoriDao.findAll(userId));
        req.setAttribute("dompets", dompetDao.findAll(userId));
        req.getRequestDispatcher(EDIT).forward(req, resp);
    }

    /** Membaca + memvalidasi form; aturannya sama dengan modul Transaksi (anti-IDOR, kategori cocok dengan jenis). */
    private TransaksiBerulang read(HttpServletRequest req, Form f, long userId) throws SQLException {
        TransaksiBerulang b = new TransaksiBerulang();
        b.setUserId(userId);
        b.setJenis(f.oneOf("jenis", "Jenis transaksi", "pemasukan", "pengeluaran"));
        b.setJudul(f.required("judul", "Nama transaksi", 255));
        b.setNominal(f.money("nominal", "Nominal", false));
        b.setFrekuensi(f.oneOf("frekuensi", "Frekuensi", "harian", "mingguan", "bulanan", "tahunan"));
        b.setTanggalBerikutnya(f.date("tanggal_berikutnya", "Tanggal berikutnya", true));
        b.setAktif("1".equals(f.str("aktif")));

        Long dompetId = Form.id(req, "dompet_id");
        if (dompetId == null || dompetDao.find(dompetId, userId) == null) {
            f.error("dompet_id", "Dompet yang dipilih tidak valid.");
        } else {
            b.setDompetId(dompetId);
        }

        if (!f.str("kategori_id").isEmpty()) {
            Long kid = Form.id(req, "kategori_id");
            Kategori k = kid == null ? null : kategoriDao.find(kid, userId);
            if (k == null) {
                f.error("kategori_id", "Kategori yang dipilih tidak valid.");
            } else if (b.getJenis() != null && !k.getJenis().equals(b.getJenis())) {
                f.error("kategori_id", "Kategori harus sesuai dengan jenis transaksi.");
            } else {
                b.setKategoriId(kid);
            }
        }
        return b;
    }

    private void create(HttpServletRequest req, HttpServletResponse resp)
            throws SQLException, ServletException, IOException {
        Form f = new Form(req);
        TransaksiBerulang b = read(req, f, uid(req));
        if (!f.ok()) {
            f.publish();
            showIndex(req, resp);
            return;
        }
        dao.insert(b);
        Flash.success(req, "Transaksi berulang berhasil ditambahkan.");
        resp.sendRedirect(req.getContextPath() + "/berulang");
    }

    private void update(HttpServletRequest req, HttpServletResponse resp)
            throws SQLException, ServletException, IOException {
        long userId = uid(req);
        Long id = Form.id(req, "id");
        TransaksiBerulang lama = id == null ? null : dao.find(id, userId);   // cek kepemilikan
        if (lama == null) {
            resp.sendError(404);
            return;
        }
        Form f = new Form(req);
        TransaksiBerulang b = read(req, f, userId);
        if (!f.ok()) {
            f.publish();
            showEdit(req, resp, lama);
            return;
        }
        b.setId(id);
        dao.update(b);
        Flash.success(req, "Transaksi berulang berhasil diperbarui.");
        resp.sendRedirect(req.getContextPath() + "/berulang");
    }

    private void toggle(HttpServletRequest req, HttpServletResponse resp) throws SQLException, IOException {
        long userId = uid(req);
        Long id = Form.id(req, "id");
        TransaksiBerulang b = id == null ? null : dao.find(id, userId);
        if (b == null) {
            resp.sendError(404);
            return;
        }
        dao.setAktif(id, userId, !b.isAktif());
        Flash.success(req, b.isAktif() ? "Transaksi berulang dijeda." : "Transaksi berulang diaktifkan kembali.");
        resp.sendRedirect(req.getContextPath() + "/berulang");
    }

    private void proses(HttpServletRequest req, HttpServletResponse resp) throws SQLException, IOException {
        int dibuat = dao.prosesJatuhTempo(uid(req), LocalDate.now());
        if (dibuat == 0) Flash.success(req, "Tidak ada transaksi berulang yang jatuh tempo.");
        else Flash.success(req, dibuat + " transaksi berhasil dibuat dari transaksi berulang. Lihat di menu Transaksi.");
        resp.sendRedirect(req.getContextPath() + "/berulang");
    }

    private void delete(HttpServletRequest req, HttpServletResponse resp) throws SQLException, IOException {
        Long id = Form.id(req, "id");
        if (id == null || !dao.delete(id, uid(req))) {
            resp.sendError(404);
            return;
        }
        Flash.success(req, "Transaksi berulang berhasil dihapus.");
        resp.sendRedirect(req.getContextPath() + "/berulang");
    }
}
