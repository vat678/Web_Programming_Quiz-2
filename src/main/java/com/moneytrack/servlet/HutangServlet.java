package com.moneytrack.servlet;

import com.moneytrack.dao.HutangPiutangDao;
import com.moneytrack.model.HutangPiutang;
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
import java.util.ArrayList;
import java.util.List;

/**
 * Modul Hutang / Piutang (CRUD + pencatatan pembayaran).
 * GET /hutang?jenis=, GET /hutang/edit?id=, POST /hutang/create, /update, /bayar, /delete.
 */
@WebServlet("/hutang/*")
public class HutangServlet extends HttpServlet {
    private static final String VIEW = "/WEB-INF/views/hutang/index.jsp";
    private static final String EDIT = "/WEB-INF/views/hutang/edit.jsp";
    private final HutangPiutangDao dao = new HutangPiutangDao();

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
                    HutangPiutang h = id == null ? null : dao.find(id, uid(req));
                    if (h == null) {
                        resp.sendError(404);
                        return;
                    }
                    req.setAttribute("hutang", h);
                    req.getRequestDispatcher(EDIT).forward(req, resp);
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
                case "/bayar" -> bayar(req, resp);
                case "/delete" -> delete(req, resp);
                default -> resp.sendError(404);
            }
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    private void showIndex(HttpServletRequest req, HttpServletResponse resp)
            throws SQLException, ServletException, IOException {
        List<HutangPiutang> semua = dao.findAll(uid(req));

        // Ringkasan dihitung dari semua data; filter jenis hanya memengaruhi daftar.
        BigDecimal sisaHutang = BigDecimal.ZERO;
        BigDecimal sisaPiutang = BigDecimal.ZERO;
        int lunas = 0;
        for (HutangPiutang h : semua) {
            if (h.isLunas()) lunas++;
            else if ("hutang".equals(h.getJenis())) sisaHutang = sisaHutang.add(h.getSisa());
            else sisaPiutang = sisaPiutang.add(h.getSisa());
        }

        String jenis = req.getParameter("jenis");
        if (!"hutang".equals(jenis) && !"piutang".equals(jenis)) jenis = "";
        List<HutangPiutang> tampil = new ArrayList<>();
        for (HutangPiutang h : semua) if (jenis.isEmpty() || jenis.equals(h.getJenis())) tampil.add(h);

        req.setAttribute("items", tampil);
        req.setAttribute("filterJenis", jenis);
        req.setAttribute("jumlahSemua", semua.size());
        req.setAttribute("sisaHutang", sisaHutang);
        req.setAttribute("sisaPiutang", sisaPiutang);
        req.setAttribute("jumlahLunas", lunas);
        req.getRequestDispatcher(VIEW).forward(req, resp);
    }

    private HutangPiutang read(Form f, long userId) {
        HutangPiutang h = new HutangPiutang();
        h.setUserId(userId);
        h.setJenis(f.oneOf("jenis", "Jenis", "hutang", "piutang"));
        h.setNamaPihak(f.required("nama_pihak", "Nama pihak", 150));
        BigDecimal nominal = f.money("nominal", "Nominal", false);
        BigDecimal terbayar = f.money("terbayar", "Jumlah terbayar", true);
        if (nominal != null && terbayar != null && terbayar.compareTo(nominal) > 0) {
            f.error("terbayar", "Jumlah terbayar tidak boleh melebihi nominal.");
        }
        h.setNominal(nominal);
        h.setTerbayar(terbayar);
        h.setJatuhTempo(f.date("jatuh_tempo", "Jatuh tempo", false));
        h.setCatatan(f.optional("catatan", "Catatan", 5000));
        return h;
    }

    private void create(HttpServletRequest req, HttpServletResponse resp)
            throws SQLException, ServletException, IOException {
        Form f = new Form(req);
        HutangPiutang h = read(f, uid(req));
        if (!f.ok()) {
            f.publish();
            showIndex(req, resp);
            return;
        }
        dao.insert(h);
        Flash.success(req, h.getJenisLabel() + " berhasil ditambahkan.");
        resp.sendRedirect(req.getContextPath() + "/hutang");
    }

    private void update(HttpServletRequest req, HttpServletResponse resp)
            throws SQLException, ServletException, IOException {
        long userId = uid(req);
        Long id = Form.id(req, "id");
        HutangPiutang lama = id == null ? null : dao.find(id, userId);   // cek kepemilikan
        if (lama == null) {
            resp.sendError(404);
            return;
        }
        Form f = new Form(req);
        HutangPiutang h = read(f, userId);
        if (!f.ok()) {
            f.publish();
            req.setAttribute("hutang", lama);
            req.getRequestDispatcher(EDIT).forward(req, resp);
            return;
        }
        h.setId(id);
        dao.update(h);
        Flash.success(req, "Data berhasil diperbarui.");
        resp.sendRedirect(req.getContextPath() + "/hutang");
    }

    private void bayar(HttpServletRequest req, HttpServletResponse resp)
            throws SQLException, ServletException, IOException {
        long userId = uid(req);
        Long id = Form.id(req, "id");
        HutangPiutang h = id == null ? null : dao.find(id, userId);
        if (h == null) {
            resp.sendError(404);
            return;
        }
        Form f = new Form(req);
        BigDecimal jumlah = f.money("jumlah", "Jumlah pembayaran", false);
        if (jumlah != null && jumlah.compareTo(h.getSisa()) > 0) {
            f.error("jumlah", "Jumlah pembayaran melebihi sisa (Rp " + h.getSisa().toBigInteger() + ").");
        }
        if (!f.ok()) {
            f.publish();
            showIndex(req, resp);
            return;
        }
        if (dao.bayar(id, userId, jumlah)) {
            boolean lunas = h.getTerbayar().add(jumlah).compareTo(h.getNominal()) >= 0;
            Flash.success(req, lunas ? "Pembayaran dicatat. " + h.getJenisLabel() + " ini sudah lunas!"
                                     : "Pembayaran berhasil dicatat.");
        } else {
            Flash.error(req, "Pembayaran gagal: jumlah melebihi sisa terbaru. Muat ulang halaman lalu coba lagi.");
        }
        resp.sendRedirect(req.getContextPath() + "/hutang");
    }

    private void delete(HttpServletRequest req, HttpServletResponse resp) throws SQLException, IOException {
        Long id = Form.id(req, "id");
        if (id == null || !dao.delete(id, uid(req))) {
            resp.sendError(404);
            return;
        }
        Flash.success(req, "Data berhasil dihapus.");
        resp.sendRedirect(req.getContextPath() + "/hutang");
    }
}
