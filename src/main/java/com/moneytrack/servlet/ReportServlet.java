package com.moneytrack.servlet;



import com.moneytrack.model.SessionUser;

import com.moneytrack.util.Db;

import jakarta.servlet.ServletException;

import jakarta.servlet.annotation.WebServlet;

import jakarta.servlet.http.HttpServlet;

import jakarta.servlet.http.HttpServletRequest;

import jakarta.servlet.http.HttpServletResponse;



import java.io.IOException;

import java.math.BigDecimal;

import java.sql.Connection;

import java.sql.PreparedStatement;

import java.sql.ResultSet;

import java.sql.SQLException;

import java.util.ArrayList;

import java.util.HashMap;

import java.util.List;

import java.util.Map;



@WebServlet("/laporan")

public class ReportServlet extends HttpServlet {

private static final String VIEW = "/WEB-INF/views/laporan/index.jsp";



private static long uid(HttpServletRequest req) {

return ((SessionUser) req.getSession().getAttribute("user")).getId();

}



@Override

protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

try {

long userId = uid(req);



BigDecimal totalPemasukan = BigDecimal.ZERO;

BigDecimal totalPengeluaran = BigDecimal.ZERO;

List<Map<String, Object>> daftarTransaksi = new ArrayList<>();



try (Connection c = Db.get()) {

String sqlSum = "SELECT jenis, SUM(nominal) AS total FROM transaksi WHERE user_id = ? GROUP BY jenis";

try (PreparedStatement ps = c.prepareStatement(sqlSum)) {

ps.setLong(1, userId);

try (ResultSet rs = ps.executeQuery()) {

while (rs.next()) {

String jenis = rs.getString("jenis");

BigDecimal total = rs.getBigDecimal("total");

if ("pemasukan".equalsIgnoreCase(jenis)) {

totalPemasukan = total != null ? total : BigDecimal.ZERO;

} else if ("pengeluaran".equalsIgnoreCase(jenis)) {

totalPengeluaran = total != null ? total : BigDecimal.ZERO;

}

}

}

}




String sqlList = "SELECT t.tanggal, t.judul AS keterangan, t.nominal AS jumlah, t.jenis, d.nama AS nama_dompet "

+ "FROM transaksi t JOIN dompet d ON t.dompet_id = d.id "

+ "WHERE t.user_id = ? ORDER BY t.tanggal DESC, t.id DESC LIMIT 50";

try (PreparedStatement ps = c.prepareStatement(sqlList)) {

ps.setLong(1, userId);

try (ResultSet rs = ps.executeQuery()) {

while (rs.next()) {

Map<String, Object> trx = new HashMap<>();

trx.put("tanggal", rs.getString("tanggal"));

trx.put("keterangan", rs.getString("keterangan"));

trx.put("jumlah", rs.getBigDecimal("jumlah"));

trx.put("jenis", rs.getString("jenis"));

trx.put("dompet", rs.getString("nama_dompet"));

daftarTransaksi.add(trx);

}

}

}

}



req.setAttribute("totalPemasukan", totalPemasukan);

req.setAttribute("totalPengeluaran", totalPengeluaran);

req.setAttribute("saldoBersih", totalPemasukan.subtract(totalPengeluaran));

req.setAttribute("daftarTransaksi", daftarTransaksi);



req.getRequestDispatcher(VIEW).forward(req, resp);

} catch (SQLException e) {

throw new ServletException(e);

}

}

}