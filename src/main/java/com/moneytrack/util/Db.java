package com.moneytrack.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/** Pool koneksi (HikariCP) + pembuatan tabel otomatis dari schema.sql. Konfigurasi lewat environment variable. */
public final class Db {
    private static HikariDataSource ds;

    private Db() {}

    private static String env(String key, String def) {
        String v = System.getenv(key);
        return (v == null || v.isBlank()) ? def : v;
    }

    public static synchronized void init() {
        if (ds != null) return;
        
        // 1. Load driver terlebih dahulu agar terdaftar di DriverManager sebelum Hikari dibuat
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Driver MySQL JDBC tidak ditemukan", e);
        }

        // 2. Inisialisasi HikariCP setelah driver siap
        HikariConfig cfg = new HikariConfig();
        cfg.setJdbcUrl(env("DB_URL",
                "jdbc:mysql://db:3306/moneytrack?createDatabaseIfNotExist=true&useSSL=false"
                        + "&allowPublicKeyRetrieval=true&serverTimezone=Asia/Jakarta&characterEncoding=UTF-8"));
        cfg.setDriverClassName("com.mysql.cj.jdbc.Driver");
        cfg.setUsername(env("DB_USER", "root"));
        cfg.setPassword(env("DB_PASS", ""));
        cfg.setMaximumPoolSize(8);
        cfg.setConnectionTimeout(15000);
        
        ds = new HikariDataSource(cfg);
    }

    public static Connection get() throws SQLException {
        if (ds == null) init();
        return ds.getConnection();
    }

    public static synchronized void shutdown() {
        if (ds != null) {
            ds.close();
            ds = null;
        }
    }

    /** Menjalankan file SQL dari classpath; pernyataan dipisah dengan titik koma di akhir baris. */
    public static void runScript(String resource) {
        try (InputStream in = Db.class.getResourceAsStream(resource)) {
            if (in == null) return;
            StringBuilder sb = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    if (line.trim().startsWith("--")) continue;
                    sb.append(line).append('\n');
                }
            }
            try (Connection c = get(); Statement st = c.createStatement()) {
                for (String sql : sb.toString().split(";\\s*\\n")) {
                    if (!sql.isBlank()) st.execute(sql);
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException("Gagal menjalankan " + resource, e);
        }
    }
}