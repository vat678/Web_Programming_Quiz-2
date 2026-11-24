CREATE TABLE IF NOT EXISTS users (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  nama VARCHAR(255) NOT NULL,
  email VARCHAR(255) NOT NULL UNIQUE,
  password_hash VARCHAR(100) NOT NULL,
  role ENUM('user','admin') NOT NULL DEFAULT 'user',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS dompet (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  nama VARCHAR(100) NOT NULL,
  tipe ENUM('tunai','bank','ewallet') NOT NULL DEFAULT 'tunai',
  saldo_awal DECIMAL(15,2) NOT NULL DEFAULT 0,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_dompet_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  INDEX idx_dompet_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS kategori (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  nama VARCHAR(100) NOT NULL,
  jenis ENUM('pemasukan','pengeluaran') NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_kategori_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  INDEX idx_kategori_user_jenis (user_id, jenis)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS transaksi (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  dompet_id BIGINT NOT NULL,
  kategori_id BIGINT NULL,
  jenis ENUM('pemasukan','pengeluaran') NOT NULL,
  judul VARCHAR(255) NOT NULL,
  nominal DECIMAL(15,2) NOT NULL,
  tanggal DATE NOT NULL,
  catatan TEXT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_transaksi_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  CONSTRAINT fk_transaksi_dompet FOREIGN KEY (dompet_id) REFERENCES dompet(id) ON DELETE RESTRICT,
  CONSTRAINT fk_transaksi_kategori FOREIGN KEY (kategori_id) REFERENCES kategori(id) ON DELETE RESTRICT,
  INDEX idx_transaksi_user_tanggal (user_id, tanggal)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS anggaran (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  kategori_id BIGINT NOT NULL,
  jumlah_anggaran DECIMAL(15,2) NOT NULL,
  bulan TINYINT UNSIGNED NOT NULL,
  tahun SMALLINT UNSIGNED NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_anggaran_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  CONSTRAINT fk_anggaran_kategori FOREIGN KEY (kategori_id) REFERENCES kategori(id) ON DELETE RESTRICT,
  UNIQUE KEY uq_anggaran_periode (user_id, kategori_id, bulan, tahun)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS target_tabungan (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  nama VARCHAR(150) NOT NULL,
  target_nominal DECIMAL(15,2) NOT NULL,
  terkumpul DECIMAL(15,2) NOT NULL DEFAULT 0,
  tenggat DATE NULL,
  catatan TEXT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_target_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  INDEX idx_target_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS hutang_piutang (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  jenis ENUM('hutang','piutang') NOT NULL,
  nama_pihak VARCHAR(150) NOT NULL,
  nominal DECIMAL(15,2) NOT NULL,
  terbayar DECIMAL(15,2) NOT NULL DEFAULT 0,
  jatuh_tempo DATE NULL,
  catatan TEXT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_hutang_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  INDEX idx_hutang_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS transaksi_berulang (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  dompet_id BIGINT NOT NULL,
  kategori_id BIGINT NULL,
  jenis ENUM('pemasukan','pengeluaran') NOT NULL,
  judul VARCHAR(255) NOT NULL,
  nominal DECIMAL(15,2) NOT NULL,
  frekuensi ENUM('harian','mingguan','bulanan','tahunan') NOT NULL DEFAULT 'bulanan',
  tanggal_berikutnya DATE NOT NULL,
  aktif TINYINT(1) NOT NULL DEFAULT 1,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_berulang_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  CONSTRAINT fk_berulang_dompet FOREIGN KEY (dompet_id) REFERENCES dompet(id) ON DELETE CASCADE,
  CONSTRAINT fk_berulang_kategori FOREIGN KEY (kategori_id) REFERENCES kategori(id) ON DELETE SET NULL,
  INDEX idx_berulang_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
