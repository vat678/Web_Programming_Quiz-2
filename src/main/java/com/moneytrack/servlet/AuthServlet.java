package com.moneytrack.servlet;

import com.moneytrack.dao.UserDao;
import com.moneytrack.model.User;
import com.moneytrack.util.Flash;
import com.moneytrack.util.Form;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.regex.Pattern;

@WebServlet({"/login", "/register", "/logout"})
public class AuthServlet extends HttpServlet {
    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private final UserDao users = new UserDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();
        if ("/login".equals(path)) {
            req.getRequestDispatcher("/WEB-INF/views/auth/login.jsp").forward(req, resp);
        } else if ("/register".equals(path)) {
            req.getRequestDispatcher("/WEB-INF/views/auth/register.jsp").forward(req, resp);
        } else {
            resp.sendRedirect(req.getContextPath() + "/");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            switch (req.getServletPath()) {
                case "/login" -> login(req, resp);
                case "/register" -> register(req, resp);
                case "/logout" -> logout(req, resp);
                default -> resp.sendError(404);
            }
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    private void login(HttpServletRequest req, HttpServletResponse resp)
            throws SQLException, ServletException, IOException {
        Form f = new Form(req);
        String email = f.required("email", "Email", 255).toLowerCase();
        String password = req.getParameter("password");
        if (password == null || password.isEmpty()) f.error("password", "Password wajib diisi.");

        if (f.ok()) {
            User u = users.findByEmail(email);
            if (u == null || !BCrypt.checkpw(password, u.getPasswordHash())) {
                f.error("email", "Email atau password salah.");
            } else {
                startSession(req, u);
                Flash.success(req, "Berhasil login.");
                resp.sendRedirect(req.getContextPath() + "/");
                return;
            }
        }
        f.publish();
        req.getRequestDispatcher("/WEB-INF/views/auth/login.jsp").forward(req, resp);
    }

    private void register(HttpServletRequest req, HttpServletResponse resp)
            throws SQLException, ServletException, IOException {
        Form f = new Form(req);
        String nama = f.required("nama", "Nama", 255);
        String email = f.required("email", "Email", 255).toLowerCase();
        if (!email.isEmpty() && !EMAIL.matcher(email).matches()) f.error("email", "Format email tidak valid.");
        String password = req.getParameter("password");
        String confirm = req.getParameter("password_confirmation");
        if (password == null || password.length() < 8) f.error("password", "Password minimal 8 karakter.");
        else if (password.length() > 72) f.error("password", "Password maksimal 72 karakter.");
        else if (!password.equals(confirm)) f.error("password", "Konfirmasi password tidak cocok.");
        if (f.ok() && users.emailExists(email)) f.error("email", "Email sudah terdaftar.");

        if (f.ok()) {
            try {
                long id = users.register(nama, email, BCrypt.hashpw(password, BCrypt.gensalt(10)));
                User u = users.findByEmail(email);
                if (u != null && u.getId() == id) {
                    startSession(req, u);
                    Flash.success(req, "Akun berhasil dibuat. Selamat datang di MoneyTrack!");
                    resp.sendRedirect(req.getContextPath() + "/");
                    return;
                }
            } catch (SQLIntegrityConstraintViolationException dup) {
                f.error("email", "Email sudah terdaftar.");
            }
        }
        f.publish();
        req.getRequestDispatcher("/WEB-INF/views/auth/register.jsp").forward(req, resp);
    }

    private void logout(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession s = req.getSession(false);
        if (s != null) s.invalidate();
        Flash.success(req, "Kamu berhasil logout.");
        resp.sendRedirect(req.getContextPath() + "/login");
    }

    /** Regenerasi ID sesi saat login (mencegah session fixation), lalu simpan data user. */
    private void startSession(HttpServletRequest req, User u) {
        req.getSession(true);
        req.changeSessionId();
        req.getSession().setAttribute("user", u.toSessionUser());
    }
}
