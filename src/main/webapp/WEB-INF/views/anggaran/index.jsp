<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:set var="posted" value="${not empty errors}"/>
<t:layout title="Anggaran" heading="Anggaran" description="Atur batas pengeluaran per kategori setiap bulan." active="anggaran">
<div class="budget-page">

    <section class="budget-card">
        <div class="budget-card-heading">
            <div>
                <h2>Tambah Anggaran</h2>
                <p>Tentukan batas pengeluaran untuk satu kategori pada bulan tertentu.</p>
            </div>
        </div>
        <form action="${ctx}/anggaran/create" method="POST">
            <input type="hidden" name="_csrf" value="${csrfToken}">
            <div class="budget-form-grid">
                <div class="budget-form-group">
                    <label for="kategori_id">Kategori Pengeluaran</label>
                    <select id="kategori_id" name="kategori_id" required>
                        <option value="">Pilih kategori</option>
                        <c:forEach items="${kategoris}" var="k">
                            <option value="${k.id}" ${posted && param.kategori_id == k.id ? 'selected' : ''}><c:out value="${k.nama}"/></option>
                        </c:forEach>
                    </select>
                    <c:if test="${empty kategoris}">
                        <small>Belum ada kategori pengeluaran. <a href="${ctx}/kategori" class="see-all">Buat kategori dulu →</a></small>
                    </c:if>
                </div>
                <div class="budget-form-group">
                    <label for="jumlah_anggaran">Jumlah Anggaran (Rp)</label>
                    <input type="number" id="jumlah_anggaran" name="jumlah_anggaran" min="1" step="0.01"
                           placeholder="Contoh: 1500000" value="<c:out value='${posted ? param.jumlah_anggaran : ""}'/>" required>
                </div>
                <div class="budget-form-group">
                    <label for="bulan">Bulan</label>
                    <select id="bulan" name="bulan" required>
                        <c:forEach items="${namaBulan}" var="nb" varStatus="st">
                            <option value="${st.count}" ${st.count == bulan ? 'selected' : ''}>${nb}</option>
                        </c:forEach>
                    </select>
                </div>
                <div class="budget-form-group">
                    <label for="tahun">Tahun</label>
                    <input type="number" id="tahun" name="tahun" min="2000" max="2100" value="${tahun}" required>
                </div>
            </div>
            <button type="submit" class="budget-button">+ Simpan Anggaran</button>
        </form>
    </section>

    <section class="budget-card">
        <div class="budget-list-heading">
            <div>
                <h2>Anggaran ${periodeLabel}</h2>
                <p>Pemakaian dihitung otomatis dari transaksi pengeluaran pada kategori terkait.</p>
            </div>
            <span class="budget-count">${anggarans.size()} anggaran</span>
        </div>

        <form method="GET" action="${ctx}/anggaran" class="filter-bar">
            <div class="form-group">
                <label for="f-bulan">Bulan</label>
                <select id="f-bulan" name="bulan">
                    <c:forEach items="${namaBulan}" var="nb" varStatus="st">
                        <option value="${st.count}" ${st.count == bulan ? 'selected' : ''}>${nb}</option>
                    </c:forEach>
                </select>
            </div>
            <div class="form-group">
                <label for="f-tahun">Tahun</label>
                <input type="number" id="f-tahun" name="tahun" min="2000" max="2100" value="${tahun}">
            </div>
            <div class="filter-actions">
                <button type="submit" class="btn-primary">Tampilkan</button>
            </div>
        </form>

        <div class="summary-chips">
            <div class="summary-chip">Total Anggaran<strong>Rp <fmt:formatNumber value="${totalAnggaran}" pattern="#,##0"/></strong></div>
            <div class="summary-chip">Terpakai<strong class="amount-expense">Rp <fmt:formatNumber value="${totalTerpakai}" pattern="#,##0"/></strong></div>
            <div class="summary-chip">Sisa<strong class="${totalSisa.signum() < 0 ? 'saldo-negatif' : 'amount-income'}">Rp <fmt:formatNumber value="${totalSisa}" pattern="#,##0"/></strong></div>
        </div>

        <div class="budget-table-wrapper">
            <table class="budget-table">
                <thead>
                    <tr><th>KATEGORI</th><th>ANGGARAN</th><th>TERPAKAI</th><th>SISA</th><th style="min-width:180px">PROGRESS</th><th>AKSI</th></tr>
                </thead>
                <tbody>
                <c:forEach items="${anggarans}" var="a">
                    <tr>
                        <td><strong><c:out value="${a.kategoriNama}"/></strong></td>
                        <td class="budget-amount">Rp <fmt:formatNumber value="${a.jumlah}" pattern="#,##0"/></td>
                        <td class="budget-amount">Rp <fmt:formatNumber value="${a.terpakai}" pattern="#,##0"/></td>
                        <td class="budget-amount ${a.sisa.signum() < 0 ? 'saldo-negatif' : ''}">Rp <fmt:formatNumber value="${a.sisa}" pattern="#,##0"/></td>
                        <td>
                            <div class="progress-bar"><div class="progress progress-${a.status}" style="width:${a.persenBar}%"></div></div>
                            <small class="progress-note progress-note-${a.status}">${a.persen}% · ${a.statusLabel}</small>
                        </td>
                        <td>
                            <div class="budget-action-buttons">
                                <a href="${ctx}/anggaran/edit?id=${a.id}" class="budget-edit-button">Edit</a>
                                <form action="${ctx}/anggaran/delete" method="POST"
                                      onsubmit="return confirm('Yakin ingin menghapus anggaran ini?')">
                                    <input type="hidden" name="_csrf" value="${csrfToken}">
                                    <input type="hidden" name="id" value="${a.id}">
                                    <button type="submit" class="budget-delete-button">Hapus</button>
                                </form>
                            </div>
                        </td>
                    </tr>
                </c:forEach>
                <c:if test="${empty anggarans}">
                    <tr><td colspan="6" class="budget-empty">Belum ada anggaran untuk ${periodeLabel}. Tambahkan lewat formulir di atas.</td></tr>
                </c:if>
                </tbody>
            </table>
        </div>
    </section>
</div>
</t:layout>
