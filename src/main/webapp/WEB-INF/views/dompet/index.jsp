<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<t:layout title="Dompet" heading="Dompet" description="Kelola dompet tunai, rekening bank, dan e-wallet." active="dompet">
<div class="transactions-page">
    <section class="content-card">
        <h2>Tambah Dompet</h2>
        <p class="section-description">Saldo setiap dompet dihitung dari saldo awal ditambah pemasukan dikurangi pengeluaran.</p>
        <form action="${ctx}/dompet/create" method="POST">
            <input type="hidden" name="_csrf" value="${csrfToken}">
            <div class="form-grid">
                <div class="form-group">
                    <label for="nama">Nama Dompet</label>
                    <input type="text" id="nama" name="nama" placeholder="Contoh: BCA, GoPay" value="<c:out value='${param.nama}'/>" maxlength="100" required>
                </div>
                <div class="form-group">
                    <label for="tipe">Tipe</label>
                    <select id="tipe" name="tipe" required>
                        <option value="tunai" ${param.tipe == 'tunai' ? 'selected' : ''}>Tunai</option>
                        <option value="bank" ${param.tipe == 'bank' ? 'selected' : ''}>Bank</option>
                        <option value="ewallet" ${param.tipe == 'ewallet' ? 'selected' : ''}>E-Wallet</option>
                    </select>
                </div>
                <div class="form-group">
                    <label for="saldo_awal">Saldo Awal (Rp)</label>
                    <input type="number" id="saldo_awal" name="saldo_awal" min="0" step="0.01" value="<c:out value='${empty param.saldo_awal ? 0 : param.saldo_awal}'/>" required>
                </div>
            </div>
            <button type="submit" class="btn-primary">+ Simpan Dompet</button>
        </form>
    </section>

    <section class="content-card">
        <div class="section-header">
            <div>
                <h2>Daftar Dompet</h2>
                <p class="section-description">Total saldo semua dompet: <strong class="${totalSaldo.signum() < 0 ? 'saldo-negatif' : ''}">Rp <fmt:formatNumber value="${totalSaldo}" pattern="#,##0"/></strong></p>
            </div>
            <span class="item-count">${dompets.size()} dompet</span>
        </div>
        <div class="table-wrapper">
            <table class="data-table">
                <thead><tr><th>NAMA</th><th>TIPE</th><th>SALDO AWAL</th><th>SALDO SAAT INI</th><th>TRANSAKSI</th><th>AKSI</th></tr></thead>
                <tbody>
                <c:forEach items="${dompets}" var="d">
                    <tr>
                        <td><strong><c:out value="${d.nama}"/></strong></td>
                        <td>${d.tipeLabel}</td>
                        <td>Rp <fmt:formatNumber value="${d.saldoAwal}" pattern="#,##0"/></td>
                        <td class="${d.saldo.signum() < 0 ? 'saldo-negatif' : ''}"><strong>Rp <fmt:formatNumber value="${d.saldo}" pattern="#,##0"/></strong></td>
                        <td>${d.jumlahTransaksi}</td>
                        <td>
                            <div class="transaction-actions">
                                <a href="${ctx}/dompet/edit?id=${d.id}" class="btn-edit">Edit</a>
                                <form action="${ctx}/dompet/delete" method="POST" style="display:inline"
                                      onsubmit="return confirm('Yakin ingin menghapus dompet ini?')">
                                    <input type="hidden" name="_csrf" value="${csrfToken}">
                                    <input type="hidden" name="id" value="${d.id}">
                                    <button type="submit" class="btn-delete">Hapus</button>
                                </form>
                            </div>
                        </td>
                    </tr>
                </c:forEach>
                <c:if test="${empty dompets}">
                    <tr><td colspan="6" class="empty-state">Belum ada dompet. Tambahkan melalui form di atas.</td></tr>
                </c:if>
                </tbody>
            </table>
        </div>
    </section>
</div>
</t:layout>
