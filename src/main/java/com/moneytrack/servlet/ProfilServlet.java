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
 * Halaman Profil: ubah nama/email dan ganti password.
 * GET /profil, POST /profil/update, POST /profil/password.
 */
@WebServlet("/profil/*")
public class ProfilServlet extends HttpServlet {
    private static final String VIEW = "/WEB-INF/views/profil/index.jsp";
    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private final UserDao users = new UserDao();

    private static long uid(HttpServletRequest req) {
        return ((SessionUser) req.getSession().getAttribute("user")).getId();
    }

    private static String path(HttpServletRequest req) {
        String p = req.getPathInfo();
        return p == null ? "/" : p;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!"/".equals(path(req))) {
            resp.sendError(404);
            return;
        }
        try {
            show(req, resp);
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            switch (path(req)) {
                case "/update" -> updateProfil(req, resp);
                case "/password" -> gantiPassword(req, resp);
                default -> resp.sendError(404);
            }
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    private void show(HttpServletRequest req, HttpServletResponse resp)
            throws SQLException, ServletException, IOException {
        long userId = uid(req);
        User u = users.findById(userId);
        if (u == null) {   // akun sudah dihapus, tetapi sesi masih ada
            req.getSession().invalidate();
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        long[] stat = users.statistik(userId);
        req.setAttribute("profil", u);
        req.setAttribute("jumlahDompet", stat[0]);
        req.setAttribute("jumlahKategori", stat[1]);
        req.setAttribute("jumlahTransaksi", stat[2]);
        req.getRequestDispatcher(VIEW).forward(req, resp);
    }

    private void updateProfil(HttpServletRequest req, HttpServletResponse resp)
            throws SQLException, ServletException, IOException {
        long userId = uid(req);
        Form f = new Form(req);
        String nama = f.required("nama", "Nama", 255);
        String email = f.required("email", "Email", 255).toLowerCase();
        if (!email.isEmpty() && !EMAIL.matcher(email).matches()) f.error("email", "Format email tidak valid.");
        if (f.ok() && users.emailDipakaiLain(email, userId)) f.error("email", "Email sudah dipakai akun lain.");

        if (f.ok()) {
            try {
                users.updateProfil(userId, nama, email);
                User u = users.findById(userId);
                req.getSession().setAttribute("user", u.toSessionUser());   // sidebar/header ikut diperbarui
                Flash.success(req, "Profil berhasil diperbarui.");
                resp.sendRedirect(req.getContextPath() + "/profil");
                return;
            } catch (SQLIntegrityConstraintViolationException dup) {
                f.error("email", "Email sudah dipakai akun lain.");
            }
        }
        f.publish();
        show(req, resp);
    }

    private void gantiPassword(HttpServletRequest req, HttpServletResponse resp)
            throws SQLException, ServletException, IOException {
        long userId = uid(req);
        Form f = new Form(req);
        String lama = req.getParameter("password_lama");
        String baru = req.getParameter("password_baru");
        String konfirmasi = req.getParameter("password_konfirmasi");

        User u = users.findById(userId);
        if (u == null) {
            resp.sendError(404);
            return;
        }
        if (lama == null || lama.isEmpty()) f.error("password_lama", "Password saat ini wajib diisi.");
        else if (!BCrypt.checkpw(lama, u.getPasswordHash())) f.error("password_lama", "Password saat ini salah.");

        if (baru == null || baru.length() < 8) f.error("password_baru", "Password baru minimal 8 karakter.");
        else if (baru.length() > 72) f.error("password_baru", "Password baru maksimal 72 karakter.");
        else if (baru.equals(lama)) f.error("password_baru", "Password baru harus berbeda dari password saat ini.");
        else if (!baru.equals(konfirmasi)) f.error("password_baru", "Konfirmasi password baru tidak cocok.");

        if (!f.ok()) {
            f.publish();
            show(req, resp);
            return;
        }
        users.updatePassword(userId, BCrypt.hashpw(baru, BCrypt.gensalt(10)));
        req.changeSessionId();   // sesi baru setelah kredensial berubah
        Flash.success(req, "Password berhasil diganti.");
        resp.sendRedirect(req.getContextPath() + "/profil");
    }
}
