<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<t:layout title="Hutang / Piutang" heading="Hutang / Piutang" description="Catat siapa berutang kepada siapa, dan pantau pelunasannya." active="hutang">
<div class="transactions-page">

    <section class="summary-cards">
        <div class="card">
            <div class="card-top"><span class="card-label">Sisa Hutang Saya</span><div class="card-icon expense-icon">↑</div></div>
            <h2>Rp <fmt:formatNumber value="${sisaHutang}" pattern="#,##0"/></h2>
            <p class="card-description">Yang masih harus kamu bayar.</p>
        </div>
        <div class="card">
            <div class="card-top"><span class="card-label">Sisa Piutang Saya</span><div class="card-icon income-icon">↓</div></div>
            <h2>Rp <fmt:formatNumber value="${sisaPiutang}" pattern="#,##0"/></h2>
            <p class="card-description">Yang masih harus dibayar orang lain kepadamu.</p>
        </div>
        <div class="card">
            <div class="card-top"><span class="card-label">Sudah Lunas</span><div class="card-icon">✓</div></div>
            <h2>${jumlahLunas} / ${jumlahSemua}</h2>
            <p class="card-description">Catatan yang sudah selesai.</p>
        </div>
    </section>

    <section class="content-card">
        <h2>Tambah Catatan</h2>
        <p class="section-description">Hutang: kamu berutang kepada orang lain. Piutang: orang lain berutang kepadamu.</p>
        <form action="${ctx}/hutang/create" method="POST">
            <input type="hidden" name="_csrf" value="${csrfToken}">
            <div class="form-grid">
                <div class="form-group">
                    <label for="jenis">Jenis</label>
                    <select id="jenis" name="jenis" required>
                        <option value="hutang" ${param.jenis == 'hutang' ? 'selected' : ''}>Hutang (saya berutang)</option>
                        <option value="piutang" ${param.jenis == 'piutang' ? 'selected' : ''}>Piutang (orang berutang ke saya)</option>
                    </select>
                </div>
                <div class="form-group">
                    <label for="nama_pihak">Nama Pihak</label>
                    <input type="text" id="nama_pihak" name="nama_pihak" maxlength="150" placeholder="Contoh: Budi"
                           value="<c:out value='${param.nama_pihak}'/>" required>
                </div>
                <div class="form-group">
                    <label for="nominal">Nominal (Rp)</label>
                    <input type="number" id="nominal" name="nominal" min="1" step="0.01"
                           value="<c:out value='${param.nominal}'/>" required>
                </div>
                <div class="form-group">
                    <label for="terbayar">Sudah Dibayar (Rp)</label>
                    <input type="number" id="terbayar" name="terbayar" min="0" step="0.01"
                           value="<c:out value='${empty param.terbayar ? 0 : param.terbayar}'/>" required>
                </div>
                <div class="form-group">
                    <label for="jatuh_tempo">Jatuh Tempo (opsional)</label>
                    <input type="date" id="jatuh_tempo" name="jatuh_tempo" value="<c:out value='${param.jatuh_tempo}'/>">
                </div>
            </div>
            <div class="form-group">
                <label for="catatan">Catatan (opsional)</label>
                <textarea id="catatan" name="catatan" rows="2" maxlength="5000"><c:out value="${param.catatan}"/></textarea>
            </div>
            <button type="submit" class="btn-primary">+ Simpan</button>
        </form>
    </section>

    <section class="content-card">
        <div class="section-header">
            <div>
                <h2>Daftar Hutang &amp; Piutang</h2>
                <p class="section-description">Yang belum lunas tampil lebih dulu.</p>
            </div>
            <span class="item-count">${items.size()} catatan</span>
        </div>

        <div class="filter-tabs">
            <a href="${ctx}/hutang" class="${empty filterJenis ? 'active' : ''}">Semua</a>
            <a href="${ctx}/hutang?jenis=hutang" class="${filterJenis == 'hutang' ? 'active' : ''}">Hutang</a>
            <a href="${ctx}/hutang?jenis=piutang" class="${filterJenis == 'piutang' ? 'active' : ''}">Piutang</a>
        </div>

        <div class="table-wrapper">
            <table class="data-table">
                <thead>
                    <tr><th>PIHAK</th><th>JENIS</th><th>NOMINAL</th><th>TERBAYAR</th><th>SISA</th><th>JATUH TEMPO</th><th style="min-width:170px">STATUS</th><th>PEMBAYARAN</th><th>AKSI</th></tr>
                </thead>
                <tbody>
                <c:forEach items="${items}" var="h">
                    <tr>
                        <td>
                            <strong><c:out value="${h.namaPihak}"/></strong>
                            <c:if test="${not empty h.catatan}"><br><small><c:out value="${h.catatan}"/></small></c:if>
                        </td>
                        <td>
                            <span class="transaction-type ${h.jenis == 'hutang' ? 'badge-expense' : 'badge-income'}">${h.jenisLabel}</span>
                        </td>
                        <td>Rp <fmt:formatNumber value="${h.nominal}" pattern="#,##0"/></td>
                        <td>Rp <fmt:formatNumber value="${h.terbayar}" pattern="#,##0"/></td>
                        <td><strong>Rp <fmt:formatNumber value="${h.sisa}" pattern="#,##0"/></strong></td>
                        <td>${h.jatuhTempoFormatted}<c:if test="${not empty h.jatuhTempoInfo}"><br><small>${h.jatuhTempoInfo}</small></c:if></td>
                        <td>
                            <span class="goal-badge goal-${h.status}">${h.statusLabel}</span>
                            <div class="progress-bar"><div class="progress ${h.lunas ? 'progress-aman' : ''}" style="width:${h.persenBar}%"></div></div>
                            <small>${h.persen}% terbayar</small>
                        </td>
                        <td>
                            <c:choose>
                                <c:when test="${h.lunas}"><small>—</small></c:when>
                                <c:otherwise>
                                    <form action="${ctx}/hutang/bayar" method="POST" class="goal-deposit">
                                        <input type="hidden" name="_csrf" value="${csrfToken}">
                                        <input type="hidden" name="id" value="${h.id}">
                                        <input type="number" name="jumlah" min="1" step="0.01" placeholder="Jumlah" aria-label="Jumlah pembayaran" required>
                                        <button type="submit" class="btn-primary">Bayar</button>
                                    </form>
                                </c:otherwise>
                            </c:choose>
                        </td>
                        <td>
                            <div class="transaction-actions">
                                <a href="${ctx}/hutang/edit?id=${h.id}" class="btn-edit">Edit</a>
                                <form action="${ctx}/hutang/delete" method="POST" style="display:inline"
                                      onsubmit="return confirm('Yakin ingin menghapus catatan ini?')">
                                    <input type="hidden" name="_csrf" value="${csrfToken}">
                                    <input type="hidden" name="id" value="${h.id}">
                                    <button type="submit" class="btn-delete">Hapus</button>
                                </form>
                            </div>
                        </td>
                    </tr>
                </c:forEach>
                <c:if test="${empty items}">
                    <tr><td colspan="9" class="empty-state">Belum ada catatan. Tambahkan lewat formulir di atas.</td></tr>
                </c:if>
                </tbody>
            </table>
        </div>
    </section>
</div>
</t:layout>
