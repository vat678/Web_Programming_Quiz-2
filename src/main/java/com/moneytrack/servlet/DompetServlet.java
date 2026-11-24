package com.moneytrack.servlet;

import com.moneytrack.dao.DompetDao;
import com.moneytrack.model.Dompet;
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

/** Modul Dompet/Rekening (CRUD). Saldo dihitung dari transaksi. */
@WebServlet("/dompet/*")
public class DompetServlet extends HttpServlet {
    private static final String VIEW = "/WEB-INF/views/dompet/index.jsp";
    private static final String EDIT = "/WEB-INF/views/dompet/edit.jsp";
    private final DompetDao dao = new DompetDao();

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
                    Dompet d = id == null ? null : dao.find(id, uid(req));
                    if (d == null) {
                        resp.sendError(404);
                        return;
                    }
                    req.setAttribute("dompet", d);
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
                case "/delete" -> delete(req, resp);
                default -> resp.sendError(404);
            }
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    private void showIndex(HttpServletRequest req, HttpServletResponse resp)
            throws SQLException, ServletException, IOException {
        var list = dao.findAll(uid(req));
        BigDecimal total = BigDecimal.ZERO;
        for (Dompet d : list) total = total.add(d.getSaldo());
        req.setAttribute("dompets", list);
        req.setAttribute("totalSaldo", total);
        req.getRequestDispatcher(VIEW).forward(req, resp);
    }

    private void create(HttpServletRequest req, HttpServletResponse resp)
            throws SQLException, ServletException, IOException {
        long userId = uid(req);
        Form f = new Form(req);
        String nama = f.required("nama", "Nama dompet", 100);
        String tipe = f.oneOf("tipe", "Tipe dompet", "tunai", "bank", "ewallet");
        BigDecimal saldoAwal = f.money("saldo_awal", "Saldo awal", true);
        if (f.ok() && dao.namaExists(userId, nama, 0)) f.error("nama", "Dompet dengan nama tersebut sudah ada.");
        if (!f.ok()) {
            f.publish();
            showIndex(req, resp);
            return;
        }
        dao.insert(userId, nama, tipe, saldoAwal);
        Flash.success(req, "Dompet berhasil ditambahkan.");
        resp.sendRedirect(req.getContextPath() + "/dompet");
    }

    private void update(HttpServletRequest req, HttpServletResponse resp)
            throws SQLException, ServletException, IOException {
        long userId = uid(req);
        Long id = Form.id(req, "id");
        Dompet lama = id == null ? null : dao.find(id, userId);
        if (lama == null) {
            resp.sendError(404);
            return;
        }
        Form f = new Form(req);
        String nama = f.required("nama", "Nama dompet", 100);
        String tipe = f.oneOf("tipe", "Tipe dompet", "tunai", "bank", "ewallet");
        BigDecimal saldoAwal = f.money("saldo_awal", "Saldo awal", true);
        if (f.ok() && dao.namaExists(userId, nama, id)) f.error("nama", "Dompet dengan nama tersebut sudah ada.");
        if (!f.ok()) {
            f.publish();
            req.setAttribute("dompet", lama);
            req.getRequestDispatcher(EDIT).forward(req, resp);
            return;
        }
        dao.update(id, userId, nama, tipe, saldoAwal);
        Flash.success(req, "Dompet berhasil diperbarui.");
        resp.sendRedirect(req.getContextPath() + "/dompet");
    }

    private void delete(HttpServletRequest req, HttpServletResponse resp) throws SQLException, IOException {
        long userId = uid(req);
        Long id = Form.id(req, "id");
        Dompet d = id == null ? null : dao.find(id, userId);
        if (d == null) {
            resp.sendError(404);
            return;
        }
        if (d.getJumlahTransaksi() > 0) {
            Flash.error(req, "Dompet tidak dapat dihapus karena masih memiliki transaksi.");
        } else if (dao.countByUser(userId) <= 1) {
            Flash.error(req, "Dompet terakhir tidak dapat dihapus. Minimal harus ada satu dompet.");
        } else {
            dao.delete(id, userId);
            Flash.success(req, "Dompet berhasil dihapus.");
        }
        resp.sendRedirect(req.getContextPath() + "/dompet");
    }
}
