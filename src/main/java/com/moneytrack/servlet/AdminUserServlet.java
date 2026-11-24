package com.moneytrack.servlet;

import com.moneytrack.dao.UserDao;
import com.moneytrack.model.SessionUser;
import com.moneytrack.model.User;
import com.moneytrack.util.Flash;
import com.moneytrack.util.Form;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.regex.Pattern;

/**
 * Modul Admin: kelola pengguna (CRUD). Hanya untuk role admin.
 * GET /admin/users, GET /admin/users/edit?id=, POST /admin/users/update, /password, /delete.
 * Aturan: admin tidak boleh menghapus/mengubah peran dirinya sendiri; harus selalu ada minimal satu admin.
 */
@WebServlet("/admin/users/*")
public class AdminUserServlet extends HttpServlet {
    private static final String VIEW = "/WEB-INF/views/admin/users.jsp";
    private static final String EDIT = "/WEB-INF/views/admin/edit.jsp";
    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private final UserDao users = new UserDao();

    private static SessionUser me(HttpServletRequest req) {
        return (SessionUser) req.getSession().getAttribute("user");
    }

    private static String path(HttpServletRequest req) {
        String p = req.getPathInfo();
        return p == null ? "/" : p;
    }

    /** Lapisan kedua di samping AuthFilter (defense in depth). */
    private boolean bukanAdmin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        SessionUser u = me(req);
        if (u == null || !u.isAdmin()) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return true;
        }
        return false;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (bukanAdmin(req, resp)) return;
        try {
            switch (path(req)) {
                case "/" -> list(req, resp);
                case "/edit" -> {
                    User target = cariTarget(req);
                    if (target == null) {
                        resp.sendError(404);
                        return;
                    }
                    tampilEdit(req, resp, target);
                }
                default -> resp.sendError(404);
            }
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (bukanAdmin(req, resp)) return;
        try {
            switch (path(req)) {
                case "/update" -> update(req, resp);
                case "/password" -> resetPassword(req, resp);
                case "/delete" -> delete(req, resp);
                default -> resp.sendError(404);
            }
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    private User cariTarget(HttpServletRequest req) throws SQLException {
        Long id = Form.id(req, "id");
        return id == null ? null : users.findById(id);
    }

    private void tampilEdit(HttpServletRequest req, HttpServletResponse resp, User target)
            throws ServletException, IOException {
        req.setAttribute("target", target);
        req.setAttribute("diriSendiri", target.getId() == me(req).getId());
        req.getRequestDispatcher(EDIT).forward(req, resp);
    }

    private void list(HttpServletRequest req, HttpServletResponse resp)
            throws SQLException, ServletException, IOException {
        String q = req.getParameter("q") == null ? "" : req.getParameter("q").trim();
        String role = req.getParameter("role");
        if (!"admin".equals(role) && !"user".equals(role)) role = "";

        req.setAttribute("daftarUser", users.cari(q, role));
        req.setAttribute("q", q);
        req.setAttribute("roleFilter", role);
        req.setAttribute("filterAktif", !q.isEmpty() || !role.isEmpty());
        req.setAttribute("totalAdmin", users.countByRole("admin"));
        req.setAttribute("totalUser", users.countByRole("user"));
        req.getRequestDispatcher(VIEW).forward(req, resp);
    }

    private void update(HttpServletRequest req, HttpServletResponse resp)
            throws SQLException, ServletException, IOException {
        User target = cariTarget(req);
        if (target == null) {
            resp.sendError(404);
            return;
        }
        long id = target.getId();
        boolean diriSendiri = id == me(req).getId();

        Form f = new Form(req);
        String nama = f.required("nama", "Nama", 255);
        String email = f.required("email", "Email", 255).toLowerCase();
        if (!email.isEmpty() && !EMAIL.matcher(email).matches()) f.error("email", "Format email tidak valid.");
        String role = f.oneOf("role", "Peran", "user", "admin");

        if (f.ok() && diriSendiri && !role.equals(target.getRole())) {
            f.error("role", "Kamu tidak dapat mengubah peran akunmu sendiri.");
        }
        if (f.ok() && target.isAdmin() && "user".equals(role) && users.countByRole("admin") <= 1) {
            f.error("role", "Harus ada minimal satu admin, peran admin terakhir tidak dapat diturunkan.");
        }
        if (f.ok() && users.emailDipakaiLain(email, id)) {
            f.error("email", "Email sudah dipakai akun lain.");
        }

        if (f.ok()) {
            try {
                users.updateOlehAdmin(id, nama, email, role);
                if (diriSendiri) {   // sidebar/header ikut diperbarui
                    req.getSession().setAttribute("user", users.findById(id).toSessionUser());
                }
                Flash.success(req, "Data pengguna berhasil diperbarui.");
                resp.sendRedirect(req.getContextPath() + "/admin/users");
                return;
            } catch (SQLIntegrityConstraintViolationException dup) {
                f.error("email", "Email sudah dipakai akun lain.");
            }
        }
        f.publish();
        tampilEdit(req, resp, target);
    }

    private void resetPassword(HttpServletRequest req, HttpServletResponse resp)
            throws SQLException, ServletException, IOException {
        User target = cariTarget(req);
        if (target == null) {
            resp.sendError(404);
            return;
        }
        Form f = new Form(req);
        String baru = req.getParameter("password_baru");
        String konfirmasi = req.getParameter("password_konfirmasi");
        if (baru == null || baru.length() < 8) f.error("password_baru", "Password baru minimal 8 karakter.");
        else if (baru.length() > 72) f.error("password_baru", "Password baru maksimal 72 karakter.");
        else if (!baru.equals(konfirmasi)) f.error("password_baru", "Konfirmasi password tidak cocok.");

        if (!f.ok()) {
            f.publish();
            tampilEdit(req, resp, target);
            return;
        }
        users.updatePassword(target.getId(), BCrypt.hashpw(baru, BCrypt.gensalt(10)));
        Flash.success(req, "Password milik " + target.getNama() + " berhasil direset.");
        resp.sendRedirect(req.getContextPath() + "/admin/users/edit?id=" + target.getId());
    }

    private void delete(HttpServletRequest req, HttpServletResponse resp) throws SQLException, IOException {
        User target = cariTarget(req);
        if (target == null) {
            resp.sendError(404);
            return;
        }
        if (target.getId() == me(req).getId()) {
            Flash.error(req, "Kamu tidak dapat menghapus akunmu sendiri.");
        } else if (target.isAdmin() && users.countByRole("admin") <= 1) {
            Flash.error(req, "Harus ada minimal satu admin, admin terakhir tidak dapat dihapus.");
        } else {
            users.hapusBeserta(target.getId());
            Flash.success(req, "Pengguna \"" + target.getNama() + "\" beserta seluruh datanya berhasil dihapus.");
        }
        resp.sendRedirect(req.getContextPath() + "/admin/users");
    }
}