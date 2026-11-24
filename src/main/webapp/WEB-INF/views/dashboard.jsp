<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
    <%@ taglib prefix="c" uri="jakarta.tags.core" %>
        <%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
            <%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
                <c:set var="ctx" value="${pageContext.request.contextPath}" />
                <t:layout title="Dashboard" heading="Dashboard"
                    description="Selamat datang kembali, ${sessionScope.user.nama}! Berikut ringkasan keuanganmu."
                    active="dashboard">

                    <section class="summary-cards">
                        <div class="card">
                            <div class="card-top">
                                <span class="card-label">Total Saldo</span>
                                <div class="card-icon">▣</div>
                            </div>
                            <h2 class="${totalSaldo.signum() < 0 ? 'saldo-negatif' : ''}">Rp
                                <fmt:formatNumber value="${totalSaldo}" pattern="#,##0" />
                            </h2>
                            <p class="card-description">Gabungan ${jumlahDompet} dompet milikmu.</p>
                        </div>
                        <div class="card">
                            <div class="card-top">
                                <span class="card-label">Pemasukan ${namaBulan}</span>
                                <div class="card-icon income-icon">↓</div>
                            </div>
                            <h2>Rp
                                <fmt:formatNumber value="${ringkasanBulan.pemasukan}" pattern="#,##0" />
                            </h2>
                            <p class="card-description"><span class="income-text">${ringkasanBulan.jumlah}</span>
                                transaksi bulan ini.</p>
                        </div>
                        <div class="card">
                            <div class="card-top">
                                <span class="card-label">Pengeluaran ${namaBulan}</span>
                                <div class="card-icon expense-icon">↑</div>
                            </div>
                            <h2>Rp
                                <fmt:formatNumber value="${ringkasanBulan.pengeluaran}" pattern="#,##0" />
                            </h2>
                            <p class="card-description">Selisih bulan ini:
                                <span class="${ringkasanBulan.saldo.signum() < 0 ? 'expense-text' : 'income-text'}">Rp
                                    <fmt:formatNumber value="${ringkasanBulan.saldo}" pattern="#,##0" />
                                </span>
                            </p>
                        </div>
                    </section>

                    <section class="middle-section">
                        <div class="card">
                            <div class="chart-heading">
                                <div>
                                    <h2 class="section-title">Arus Kas 6 Bulan Terakhir</h2>
                                    <p class="section-description">Perbandingan pemasukan dan pengeluaran per bulan.</p>
                                </div>
                            </div>
                            <div class="chart">
                                <c:forEach items="${perBulan}" var="b">
                                    <div class="month-column">
                                        <div class="bars">
                                            <div class="bar bar-income" style="height:${b.tinggiPemasukan}%"
                                                title="Pemasukan Rp <fmt:formatNumber value='${b.pemasukan}' pattern='#,##0'/>">
                                            </div>
                                            <div class="bar bar-expense" style="height:${b.tinggiPengeluaran}%"
                                                title="Pengeluaran Rp <fmt:formatNumber value='${b.pengeluaran}' pattern='#,##0'/>">
                                            </div>
                                        </div>
                                        <span class="month-label">
                                            <c:out value="${b.label}" />
                                        </span>
                                    </div>
                                </c:forEach>
                            </div>
                            <div class="legend">
                                <div class="legend-item"><span class="legend-color"
                                        style="background:#aaa0ff"></span>Pemasukan</div>
                                <div class="legend-item"><span class="legend-color"
                                        style="background:#6655ff"></span>Pengeluaran</div>
                            </div>
                        </div>

                        <div class="card">
                            <h2 class="section-title">Pengeluaran per Kategori</h2>
                            <p class="section-description">Lima kategori terbesar pada ${namaBulan}.</p>
                            <div class="category-list">
                                <c:forEach items="${topKategori}" var="k">
                                    <div class="category-item">
                                        <div class="category-icon">▤</div>
                                        <div class="category-info">
                                            <strong>
                                                <c:out value="${k.nama}" />
                                            </strong>
                                            <div class="progress-bar">
                                                <div class="progress" style="width:${k.persen}%"></div>
                                            </div>
                                        </div>
                                        <span class="category-amount">Rp
                                            <fmt:formatNumber value="${k.total}" pattern="#,##0" />
                                        </span>
                                    </div>
                                </c:forEach>
                                <c:if test="${empty topKategori}">
                                    <p class="empty-state">Belum ada pengeluaran bulan ini.</p>
                                </c:if>
                            </div>
                        </div>
                    </section>

                    <section class="card transactions-card">
                        <div class="table-heading">
                            <div>
                                <h2 class="section-title">Transaksi Terbaru</h2>
                                <p class="section-description">Lima transaksi terakhir yang kamu catat.</p>
                            </div>
                            <a href="${ctx}/transaksi" class="see-all">Lihat semua →</a>
                        </div>
                        <div class="table-wrapper">
                            <table>
                                <thead>
                                    <tr>
                                        <th>TRANSAKSI</th>
                                        <th>KATEGORI</th>
                                        <th>DOMPET</th>
                                        <th>TANGGAL</th>
                                        <th>NOMINAL</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:forEach items="${terbaru}" var="tr">
                                        <tr>
                                            <td><strong>
                                                    <c:out value="${tr.judul}" />
                                                </strong></td>
                                            <td>
                                                <c:out
                                                    value="${empty tr.kategoriNama ? 'Tanpa kategori' : tr.kategoriNama}" />
                                            </td>
                                            <td>
                                                <c:out value="${tr.dompetNama}" />
                                            </td>
                                            <td>${tr.tanggalFormatted}</td>
                                            <td class="${tr.jenis == 'pemasukan' ? 'amount-income' : 'amount-expense'}">
                                                ${tr.jenis == 'pemasukan' ? '+' : '-'} Rp
                                                <fmt:formatNumber value="${tr.nominal}" pattern="#,##0" />
                                            </td>
                                        </tr>
                                    </c:forEach>
                                    <c:if test="${empty terbaru}">
                                        <tr>
                                            <td colspan="5" class="empty-state">Belum ada transaksi. <a
                                                    href="${ctx}/transaksi" class="see-all">Tambah transaksi pertama
                                                    →</a></td>
                                        </tr>
                                    </c:if>
                                </tbody>
                            </table>
                        </div>
                    </section>
                </t:layout>