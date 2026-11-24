package com.moneytrack.servlet;

import com.moneytrack.dao.KategoriDao;
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
import java.sql.SQLException;

/**
 * Modul Kategori (CRUD penuh).
 * GET /kategori, GET /kategori/edit?id=, POST /kategori/create, /update, /delete.
 */
@WebServlet("/kategori/*")
public class KategoriServlet extends HttpServlet {
    private static final String VIEW = "/WEB-INF/views/kategori/index.jsp";
    private static final String EDIT = "/WEB-INF/views/kategori/edit.jsp";
    private final KategoriDao dao = new KategoriDao();

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
                case "/" -> {
                    req.setAttribute("kategoris", dao.findAll(uid(req)));
                    req.getRequestDispatcher(VIEW).forward(req, resp);
                }
                case "/edit" -> {
                    Long id = Form.id(req, "id");
                    Kategori k = id == null ? null : dao.find(id, uid(req));
                    if (k == null) {
                        resp.sendError(404);
                        return;
                    }
                    req.setAttribute("kategori", k);
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

    private void create(HttpServletRequest req, HttpServletResponse resp)
            throws SQLException, ServletException, IOException {
        long userId = uid(req);
        Form f = new Form(req);
        String nama = f.required("nama", "Nama kategori", 100);
        String jenis = f.oneOf("jenis", "Jenis kategori", "pemasukan", "pengeluaran");
        if (f.ok() && dao.namaExists(userId, nama, jenis, 0)) {
            f.error("nama", "Kategori dengan nama tersebut sudah ada.");
        }
        if (!f.ok()) {
            f.publish();
            req.setAttribute("kategoris", dao.findAll(userId));
            req.getRequestDispatcher(VIEW).forward(req, resp);
            return;
        }
        dao.insert(userId, nama, jenis);
        Flash.success(req, "Kategori berhasil ditambahkan!");
        resp.sendRedirect(req.getContextPath() + "/kategori");
    }

    private void update(HttpServletRequest req, HttpServletResponse resp)
            throws SQLException, ServletException, IOException {
        long userId = uid(req);
        Long id = Form.id(req, "id");
        Kategori lama = id == null ? null : dao.find(id, userId);   // cek kepemilikan (anti-IDOR)
        if (lama == null) {
            resp.sendError(404);
            return;
        }
        Form f = new Form(req);
        String nama = f.required("nama", "Nama kategori", 100);
        String jenis = f.oneOf("jenis", "Jenis kategori", "pemasukan", "pengeluaran");
        if (f.ok() && jenis != null && !jenis.equals(lama.getJenis()) && lama.isDipakai()) {
            f.error("jenis", "Jenis tidak dapat diubah karena kategori masih dipakai transaksi atau anggaran.");
        }
        if (f.ok() && dao.namaExists(userId, nama, jenis, id)) {
            f.error("nama", "Kategori dengan nama tersebut sudah ada.");
        }
        if (!f.ok()) {
            f.publish();
            req.setAttribute("kategori", lama);
            req.getRequestDispatcher(EDIT).forward(req, resp);
            return;
        }
        dao.update(id, userId, nama, jenis);
        Flash.success(req, "Kategori berhasil diperbarui.");
        resp.sendRedirect(req.getContextPath() + "/kategori");
    }

    private void delete(HttpServletRequest req, HttpServletResponse resp) throws SQLException, IOException {
        long userId = uid(req);
        Long id = Form.id(req, "id");
        Kategori k = id == null ? null : dao.find(id, userId);
        if (k == null) {
            resp.sendError(404);
            return;
        }
        if (k.isDipakai()) {
            Flash.error(req, "Kategori tidak dapat dihapus karena masih digunakan oleh transaksi atau anggaran.");
        } else {
            dao.delete(id, userId);
            Flash.success(req, "Kategori berhasil dihapus!");
        }
        resp.sendRedirect(req.getContextPath() + "/kategori");
    }
}
