package com.moneytrack.servlet;

import com.moneytrack.dao.DashboardDao;
import com.moneytrack.dao.DompetDao;
import com.moneytrack.dao.TransaksiDao;
import com.moneytrack.model.Dompet;
import com.moneytrack.model.SessionUser;
import com.moneytrack.model.TransaksiFilter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

/** Dashboard: saldo total, ringkasan bulan ini, grafik 6 bulan, pengeluaran per kategori, transaksi terbaru. */
@WebServlet("")
public class DashboardServlet extends HttpServlet {
    private final DompetDao dompetDao = new DompetDao();
    private final TransaksiDao transaksiDao = new TransaksiDao();
    private final DashboardDao dashboardDao = new DashboardDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        long userId = ((SessionUser) req.getSession().getAttribute("user")).getId();
        try {
            YearMonth bulanIni = YearMonth.now();
            LocalDate awal = bulanIni.atDay(1);
            LocalDate akhir = bulanIni.atEndOfMonth();

            List<Dompet> dompets = dompetDao.findAll(userId);
            BigDecimal totalSaldo = BigDecimal.ZERO;
            for (Dompet d : dompets) totalSaldo = totalSaldo.add(d.getSaldo());

            TransaksiFilter bulanFilter = new TransaksiFilter();
            bulanFilter.setDari(awal);
            bulanFilter.setSampai(akhir);

            req.setAttribute("totalSaldo", totalSaldo);
            req.setAttribute("jumlahDompet", dompets.size());
            req.setAttribute("ringkasanBulan", transaksiDao.ringkasan(userId, bulanFilter));
            req.setAttribute("namaBulan", bulanIni.getMonth().getDisplayName(TextStyle.FULL, Locale.forLanguageTag("id-ID"))
                    + " " + bulanIni.getYear());
            req.setAttribute("perBulan", dashboardDao.perBulan(userId, 6));
            req.setAttribute("topKategori", dashboardDao.pengeluaranPerKategori(userId, awal, akhir, 5));
            req.setAttribute("terbaru", transaksiDao.findPage(userId, new TransaksiFilter(), 0, 5));
        } catch (SQLException e) {
            throw new ServletException(e);
        }
        req.getRequestDispatcher("/WEB-INF/views/dashboard.jsp").forward(req, resp);
    }
}
