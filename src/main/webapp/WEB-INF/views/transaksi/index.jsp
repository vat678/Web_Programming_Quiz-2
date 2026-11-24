<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:set var="posted" value="${not empty errors}"/>
<c:set var="qs" value="${empty filter.queryString ? '' : filter.queryString.concat('&')}"/>
<t:layout title="Transaksi" heading="Transaksi" description="Kelola pemasukan dan pengeluaranmu." active="transaksi">
<section class="transactions-page">

    <div class="card transaction-form-card">
        <h2 class="section-title">Tambah Transaksi</h2>
        <p class="section-description">Catat pemasukan atau pengeluaran keuanganmu.</p>
        <t:transaksi-form action="${ctx}/transaksi/create" label="+ Simpan Transaksi"
            jenisVal="${posted ? param.jenis : ''}" dompetVal="${posted ? param.dompet_id : ''}"
            kategoriVal="${posted ? param.kategori_id : ''}" judulVal="${posted ? param.judul : ''}"
            nominalVal="${posted ? param.nominal : ''}" tanggalVal="${posted ? param.tanggal : today}"
            catatanVal="${posted ? param.catatan : ''}" kategoris="${kategoris}" dompets="${dompets}"/>
    </div>

    <div class="card transactions-card">
        <div class="table-heading">
            <div>
                <h2 class="section-title">Daftar Transaksi</h2>
                <p class="section-description">Cari dan saring transaksi yang tersimpan.</p>
            </div>
            <span class="transaction-count">${ringkasan.jumlah} transaksi</span>
        </div>

        <form method="GET" action="${ctx}/transaksi" class="filter-bar">
            <div class="form-group">
                <label for="f-q">Cari</label>
                <input type="text" id="f-q" name="q" value="<c:out value='${filter.q}'/>" placeholder="Judul atau catatan">
            </div>
            <div class="form-group">
                <label for="f-jenis">Jenis</label>
                <select id="f-jenis" name="jenis">
                    <option value="">Semua</option>
                    <option value="pemasukan" ${filter.jenis == 'pemasukan' ? 'selected' : ''}>Pemasukan</option>
                    <option value="pengeluaran" ${filter.jenis == 'pengeluaran' ? 'selected' : ''}>Pengeluaran</option>
                </select>
            </div>
            <div class="form-group">
                <label for="f-kategori">Kategori</label>
                <select id="f-kategori" name="kategori">
                    <option value="">Semua</option>
                    <c:forEach items="${kategoris}" var="k">
                        <option value="${k.id}" ${filter.kategoriId == k.id ? 'selected' : ''}><c:out value="${k.nama}"/></option>
                    </c:forEach>
                </select>
            </div>
            <div class="form-group">
                <label for="f-dompet">Dompet</label>
                <select id="f-dompet" name="dompet">
                    <option value="">Semua</option>
                    <c:forEach items="${dompets}" var="d">
                        <option value="${d.id}" ${filter.dompetId == d.id ? 'selected' : ''}><c:out value="${d.nama}"/></option>
                    </c:forEach>
                </select>
            </div>
            <div class="form-group">
                <label for="f-dari">Dari tanggal</label>
                <input type="date" id="f-dari" name="dari" value="${filter.dari}">
            </div>
            <div class="form-group">
                <label for="f-sampai">Sampai tanggal</label>
                <input type="date" id="f-sampai" name="sampai" value="${filter.sampai}">
            </div>
            <div class="filter-actions">
                <button type="submit" class="btn-primary">Terapkan</button>
                <c:if test="${filter.aktif}"><a href="${ctx}/transaksi" class="btn-edit">Reset</a></c:if>
            </div>
        </form>

        <div class="summary-chips">
            <div class="summary-chip">Pemasukan<strong class="amount-income">Rp <fmt:formatNumber value="${ringkasan.pemasukan}" pattern="#,##0"/></strong></div>
            <div class="summary-chip">Pengeluaran<strong class="amount-expense">Rp <fmt:formatNumber value="${ringkasan.pengeluaran}" pattern="#,##0"/></strong></div>
            <div class="summary-chip">Selisih<strong class="${ringkasan.saldo.signum() < 0 ? 'saldo-negatif' : ''}">Rp <fmt:formatNumber value="${ringkasan.saldo}" pattern="#,##0"/></strong></div>
        </div>

        <div class="table-wrapper">
            <table>
                <thead>
                    <tr><th>TRANSAKSI</th><th>KATEGORI</th><th>DOMPET</th><th>TANGGAL</th><th>JENIS</th><th>NOMINAL</th><th>AKSI</th></tr>
                </thead>
                <tbody>
                <c:forEach items="${transaksis}" var="tr">
                    <tr>
                        <td>
                            <strong><c:out value="${tr.judul}"/></strong>
                            <c:if test="${not empty tr.catatan}"><br><small><c:out value="${tr.catatan}"/></small></c:if>
                        </td>
                        <td><c:out value="${empty tr.kategoriNama ? 'Tanpa kategori' : tr.kategoriNama}"/></td>
                        <td><c:out value="${tr.dompetNama}"/></td>
                        <td>${tr.tanggalFormatted}</td>
                        <td>
                            <c:choose>
                                <c:when test="${tr.jenis == 'pemasukan'}"><span class="transaction-type badge-income">Pemasukan</span></c:when>
                                <c:otherwise><span class="transaction-type badge-expense">Pengeluaran</span></c:otherwise>
                            </c:choose>
                        </td>
                        <td class="${tr.jenis == 'pemasukan' ? 'amount-income' : 'amount-expense'}">
                            ${tr.jenis == 'pemasukan' ? '+' : '-'} Rp <fmt:formatNumber value="${tr.nominal}" pattern="#,##0"/>
                        </td>
                        <td>
                            <div class="transaction-actions">
                                <a href="${ctx}/transaksi/edit?id=${tr.id}" class="btn-edit">Edit</a>
                                <form action="${ctx}/transaksi/delete" method="POST" style="display:inline"
                                      onsubmit="return confirm('Yakin ingin menghapus transaksi ini?')">
                                    <input type="hidden" name="_csrf" value="${csrfToken}">
                                    <input type="hidden" name="id" value="${tr.id}">
                                    <button type="submit" class="btn-delete">Hapus</button>
                                </form>
                            </div>
                        </td>
                    </tr>
                </c:forEach>
                <c:if test="${empty transaksis}">
                    <tr><td colspan="7" class="empty-state">
                        <c:choose>
                            <c:when test="${filter.aktif}">Tidak ada transaksi yang cocok dengan filter.</c:when>
                            <c:otherwise>Belum ada transaksi. Silakan tambahkan transaksi melalui formulir di atas.</c:otherwise>
                        </c:choose>
                    </td></tr>
                </c:if>
                </tbody>
            </table>
        </div>

        <c:if test="${totalPages > 1}">
            <div class="pagination">
                <c:if test="${page > 1}"><a href="<c:out value='${ctx}/transaksi?${qs}page=${page - 1}'/>">‹ Sebelumnya</a></c:if>
                <span class="page-info">Halaman ${page} dari ${totalPages}</span>
                <c:if test="${page < totalPages}"><a href="<c:out value='${ctx}/transaksi?${qs}page=${page + 1}'/>">Berikutnya ›</a></c:if>
            </div>
        </c:if>
    </div>
</section>
</t:layout>
