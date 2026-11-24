package com.moneytrack.servlet;

import com.moneytrack.dao.TargetTabunganDao;
import com.moneytrack.model.SessionUser;
import com.moneytrack.model.TargetTabungan;
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
import java.util.List;

/**
 * Modul Target Tabungan (CRUD + setoran).
 * GET /tabungan, GET /tabungan/edit?id=, POST /tabungan/create, /update, /delete, /setor.
 */
@WebServlet("/tabungan/*")
public class TabunganServlet extends HttpServlet {
    private static final String VIEW = "/WEB-INF/views/tabungan/index.jsp";
    private static final String EDIT = "/WEB-INF/views/tabungan/edit.jsp";
    private final TargetTabunganDao dao = new TargetTabunganDao();

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
                    TargetTabungan t = id == null ? null : dao.find(id, uid(req));
                    if (t == null) {
                        resp.sendError(404);
                        return;
                    }
                    req.setAttribute("target", t);
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
                case "/setor" -> setor(req, resp);
                case "/delete" -> delete(req, resp);
                default -> resp.sendError(404);
            }
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    private void showIndex(HttpServletRequest req, HttpServletResponse resp)
            throws SQLException, ServletException, IOException {
        List<TargetTabungan> list = dao.findAll(uid(req));
        BigDecimal totalTarget = BigDecimal.ZERO;
        BigDecimal totalTerkumpul = BigDecimal.ZERO;
        int tercapai = 0;
        for (TargetTabungan t : list) {
            totalTarget = totalTarget.add(t.getTargetNominal());
            totalTerkumpul = totalTerkumpul.add(t.getTerkumpul());
            if (t.isTercapai()) tercapai++;
        }
        req.setAttribute("targets", list);
        req.setAttribute("totalTarget", totalTarget);
        req.setAttribute("totalTerkumpul", totalTerkumpul);
        req.setAttribute("jumlahTercapai", tercapai);
        req.getRequestDispatcher(VIEW).forward(req, resp);
    }

    /** Membaca + memvalidasi input form target; nilai bisa null jika ada error di Form. */
    private TargetTabungan read(HttpServletRequest req, Form f, long userId) {
        TargetTabungan t = new TargetTabungan();
        t.setUserId(userId);
        t.setNama(f.required("nama", "Nama target", 150));
        t.setTargetNominal(f.money("target_nominal", "Target nominal", false));
        t.setTerkumpul(f.money("terkumpul", "Jumlah terkumpul", true));
        t.setTenggat(f.date("tenggat", "Tenggat", false));
        t.setCatatan(f.optional("catatan", "Catatan", 5000));
        return t;
    }

    private void create(HttpServletRequest req, HttpServletResponse resp)
            throws SQLException, ServletException, IOException {
        long userId = uid(req);
        Form f = new Form(req);
        TargetTabungan t = read(req, f, userId);
        if (!f.ok()) {
            f.publish();
            showIndex(req, resp);
            return;
        }
        dao.insert(t);
        Flash.success(req, "Target tabungan berhasil ditambahkan.");
        resp.sendRedirect(req.getContextPath() + "/tabungan");
    }

    private void update(HttpServletRequest req, HttpServletResponse resp)
            throws SQLException, ServletException, IOException {
        long userId = uid(req);
        Long id = Form.id(req, "id");
        TargetTabungan lama = id == null ? null : dao.find(id, userId);   // cek kepemilikan
        if (lama == null) {
            resp.sendError(404);
            return;
        }
        Form f = new Form(req);
        TargetTabungan t = read(req, f, userId);
        if (!f.ok()) {
            f.publish();
            req.setAttribute("target", lama);
            req.getRequestDispatcher(EDIT).forward(req, resp);
            return;
        }
        t.setId(id);
        dao.update(t);
        Flash.success(req, "Target tabungan berhasil diperbarui.");
        resp.sendRedirect(req.getContextPath() + "/tabungan");
    }

    private void setor(HttpServletRequest req, HttpServletResponse resp)
            throws SQLException, ServletException, IOException {
        long userId = uid(req);
        Long id = Form.id(req, "id");
        TargetTabungan t = id == null ? null : dao.find(id, userId);
        if (t == null) {
            resp.sendError(404);
            return;
        }
        Form f = new Form(req);
        BigDecimal nominal = f.money("nominal", "Nominal setoran", false);
        if (!f.ok()) {
            f.publish();
            showIndex(req, resp);
            return;
        }
        dao.tambahSetoran(id, userId, nominal);
        boolean tercapai = t.getTerkumpul().add(nominal).compareTo(t.getTargetNominal()) >= 0;
        Flash.success(req, tercapai && !t.isTercapai()
                ? "Setoran dicatat. Selamat, target \"" + t.getNama() + "\" tercapai!"
                : "Setoran berhasil dicatat.");
        resp.sendRedirect(req.getContextPath() + "/tabungan");
    }

    private void delete(HttpServletRequest req, HttpServletResponse resp) throws SQLException, IOException {
        Long id = Form.id(req, "id");
        if (id == null || !dao.delete(id, uid(req))) {
            resp.sendError(404);
            return;
        }
        Flash.success(req, "Target tabungan berhasil dihapus.");
        resp.sendRedirect(req.getContextPath() + "/tabungan");
    }
}
