<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
    <%@ taglib prefix="c" uri="jakarta.tags.core" %>
        <%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
            <%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
                <t:layout title="Laporan" heading="Laporan Keuangan"
                    description="Rekapitulasi dan ringkasan transaksi keuangan pribadimu." active="laporan">

                    <section class="summary-cards">
                        <div class="card">
                            <div class="card-top">
                                <span class="card-label">Total Pemasukan</span>
                                <div class="card-icon income-icon">↓</div>
                            </div>
                            <h2>Rp
                                <fmt:formatNumber value="${totalPemasukan}" pattern="#,##0" />
                            </h2>
                            <p class="card-description">Seluruh pemasukan yang tercatat.</p>
                        </div>
                        <div class="card">
                            <div class="card-top">
                                <span class="card-label">Total Pengeluaran</span>
                                <div class="card-icon expense-icon">↑</div>
                            </div>
                            <h2>Rp
                                <fmt:formatNumber value="${totalPengeluaran}" pattern="#,##0" />
                            </h2>
                            <p class="card-description">Seluruh pengeluaran yang tercatat.</p>
                        </div>
                        <div class="card">
                            <div class="card-top">
                                <span class="card-label">Saldo Bersih</span>
                                <div class="card-icon">▣</div>
                            </div>
                            <h2 class="${saldoBersih < 0 ? 'saldo-negatif' : ''}">Rp
                                <fmt:formatNumber value="${saldoBersih}" pattern="#,##0" />
                            </h2>
                            <p class="card-description">Pemasukan dikurangi pengeluaran.</p>
                        </div>
                    </section>

                    <section class="card transactions-card">
                        <div class="table-heading">
                            <div>
                                <h2 class="section-title">Riwayat Transaksi</h2>
                                <p class="section-description">50 transaksi terakhir yang kamu catat.</p>
                            </div>
                        </div>
                        <div class="table-wrapper">
                            <table>
                                <thead>
                                    <tr>
                                        <th>TANGGAL</th>
                                        <th>KETERANGAN</th>
                                        <th>DOMPET</th>
                                        <th>JENIS</th>
                                        <th>NOMINAL</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:forEach items="${daftarTransaksi}" var="tr">
                                        <tr>
                                            <td>
                                                <c:out value="${tr.tanggal}" />
                                            </td>
                                            <td><strong>
                                                    <c:out value="${tr.keterangan}" />
                                                </strong></td>
                                            <td>
                                                <c:out value="${tr.dompet}" />
                                            </td>
                                            <td>
                                                <c:out
                                                    value="${tr.jenis == 'pemasukan' ? 'Pemasukan' : 'Pengeluaran'}" />
                                            </td>
                                            <td class="${tr.jenis == 'pemasukan' ? 'amount-income' : 'amount-expense'}">
                                                ${tr.jenis == 'pemasukan' ? '+' : '-'} Rp
                                                <fmt:formatNumber value="${tr.jumlah}" pattern="#,##0" />
                                            </td>
                                        </tr>
                                    </c:forEach>
                                    <c:if test="${empty daftarTransaksi}">
                                        <tr>
                                            <td colspan="5" class="empty-state">Belum ada data transaksi.</td>
                                        </tr>
                                    </c:if>
                                </tbody>
                            </table>
                        </div>
                    </section>
                </t:layout>