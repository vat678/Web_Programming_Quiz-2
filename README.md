# MoneyTrack (JSP)

Rewrite MoneyTrack dari Laravel ke Jakarta Servlet + JSP + JSTL + JDBC (HikariCP) + MySQL.

## Cara 1 (paling mudah): Docker
Prasyarat: Docker Desktop.

    docker compose up --build

Buka http://localhost:8080 (build pertama beberapa menit). Berhenti: `Ctrl+C`; reset data: `docker compose down -v`.

## Cara 2: manual
Prasyarat: Java 17, Maven, MySQL 8, Tomcat 10.1.

1. Pastikan MySQL berjalan (database `moneytrack` dibuat otomatis).
2. Environment variable bila perlu: `DB_URL`, `DB_USER`, `DB_PASS`, `ADMIN_EMAIL`, `ADMIN_PASS`.
   Default: `root` tanpa password di `localhost:3306`.
3. `mvn package`, salin `target/ROOT.war` ke folder `webapps/` Tomcat, jalankan Tomcat.
4. Buka http://localhost:8080

Tabel dibuat otomatis dari `schema.sql`. Admin awal: `admin@moneytrack.local` / `admin12345`.
