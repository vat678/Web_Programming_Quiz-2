package com.moneytrack.servlet;

import com.moneytrack.dao.UserDao;
import com.moneytrack.util.Db;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import org.mindrot.jbcrypt.BCrypt;

/** Saat aplikasi start: buat tabel (schema.sql) dan akun admin awal bila belum ada. */
@WebListener
public class AppInit implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        Db.init();
        Db.runScript("/schema.sql");
        try {
            UserDao dao = new UserDao();
            if (dao.countByRole("admin") == 0) {
                String email = System.getenv().getOrDefault("ADMIN_EMAIL", "admin@moneytrack.local");
                String pass = System.getenv().getOrDefault("ADMIN_PASS", "admin12345");
                dao.createAdmin("Administrator", email, BCrypt.hashpw(pass, BCrypt.gensalt(10)));
            }
        } catch (Exception e) {
            throw new IllegalStateException("Gagal membuat admin awal", e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        Db.shutdown();
    }
}
