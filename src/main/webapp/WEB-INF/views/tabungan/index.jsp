<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<t:layout title="Target Tabungan" heading="Target Tabungan" description="Wujudkan tujuan keuanganmu selangkah demi selangkah." active="tabungan">
<div class="transactions-page">

    <section class="summary-cards">
        <div class="card">
            <div class="card-top"><span class="card-label">Total Target</span><div class="card-icon">★</div></div>
            <h2>Rp <fmt:formatNumber value="${totalTarget}" pattern="#,##0"/></h2>
            <p class="card-description">${targets.size()} target tabungan.</p>
        </div>
        <div class="card">
            <div class="card-top"><span class="card-label">Sudah Terkumpul</span><div class="card-icon income-icon">↓</div></div>
            <h2>Rp <fmt:formatNumber value="${totalTerkumpul}" pattern="#,##0"/></h2>
            <p class="card-description">Dari seluruh target yang kamu buat.</p>
        </div>
        <div class="card">
            <div class="card-top"><span class="card-label">Target Tercapai</span><div class="card-icon">✓</div></div>
            <h2>${jumlahTercapai} / ${targets.size()}</h2>
            <p class="card-description">Terus semangat menabung!</p>
        </div>
    </section>

    <section class="content-card">
        <h2>Tambah Target</h2>
        <p class="section-description">Contoh: Dana Darurat, Laptop Baru, Liburan.</p>
        <form action="${ctx}/tabungan/create" method="POST">
            <input type="hidden" name="_csrf" value="${csrfToken}">
            <div class="form-grid">
                <div class="form-group">
                    <label for="nama">Nama Target</label>
                    <input type="text" id="nama" name="nama" maxlength="150" placeholder="Contoh: Laptop Baru"
                           value="<c:out value='${param.nama}'/>" required>
                </div>
                <div class="form-group">
                    <label for="target_nominal">Target Nominal (Rp)</label>
                    <input type="number" id="target_nominal" name="target_nominal" min="1" step="0.01"
                           value="<c:out value='${param.target_nominal}'/>" required>
                </div>
                <div class="form-group">
                    <label for="terkumpul">Sudah Terkumpul (Rp)</label>
                    <input type="number" id="terkumpul" name="terkumpul" min="0" step="0.01"
                           value="<c:out value='${empty param.terkumpul ? 0 : param.terkumpul}'/>" required>
                </div>
                <div class="form-group">
                    <label for="tenggat">Tenggat (opsional)</label>
                    <input type="date" id="tenggat" name="tenggat" value="<c:out value='${param.tenggat}'/>">
                </div>
            </div>
            <div class="form-group">
                <label for="catatan">Catatan (opsional)</label>
                <textarea id="catatan" name="catatan" rows="2" maxlength="5000"><c:out value="${param.catatan}"/></textarea>
            </div>
            <button type="submit" class="btn-primary">+ Simpan Target</button>
        </form>
    </section>

    <section>
        <div class="section-header">
            <div>
                <h2 class="section-title">Daftar Target</h2>
                <p class="section-description">Catat setoran langsung di setiap kartu.</p>
            </div>
            <span class="item-count">${targets.size()} target</span>
        </div>

        <c:if test="${empty targets}">
            <div class="card"><p class="empty-state">Belum ada target tabungan. Buat target pertamamu lewat formulir di atas.</p></div>
        </c:if>

        <div class="goal-grid">
            <c:forEach items="${targets}" var="g">
                <article class="card goal-card">
                    <div class="goal-head">
                        <h3><c:out value="${g.nama}"/></h3>
                        <span class="goal-badge goal-${g.status}">${g.statusLabel}</span>
                    </div>
                    <div class="goal-amounts">
                        <strong>Rp <fmt:formatNumber value="${g.terkumpul}" pattern="#,##0"/></strong>
                        <span>dari Rp <fmt:formatNumber value="${g.targetNominal}" pattern="#,##0"/></span>
                    </div>
                    <div class="progress-bar"><div class="progress ${g.tercapai ? 'progress-aman' : ''}" style="width:${g.persenBar}%"></div></div>
                    <div class="goal-meta">
                        <span>${g.persen}%</span>
                        <span>Kurang Rp <fmt:formatNumber value="${g.sisa}" pattern="#,##0"/></span>
                    </div>
                    <p class="goal-deadline">${g.tenggatInfo}</p>
                    <c:if test="${not empty g.catatan}"><p class="goal-note"><c:out value="${g.catatan}"/></p></c:if>

                    <form action="${ctx}/tabungan/setor" method="POST" class="goal-deposit">
                        <input type="hidden" name="_csrf" value="${csrfToken}">
                        <input type="hidden" name="id" value="${g.id}">
                        <input type="number" name="nominal" min="1" step="0.01" placeholder="Nominal setoran" aria-label="Nominal setoran" required>
                        <button type="submit" class="btn-primary">+ Setor</button>
                    </form>

                    <div class="transaction-actions">
                        <a href="${ctx}/tabungan/edit?id=${g.id}" class="btn-edit">Edit</a>
                        <form action="${ctx}/tabungan/delete" method="POST" style="display:inline"
                              onsubmit="return confirm('Yakin ingin menghapus target ini?')">
                            <input type="hidden" name="_csrf" value="${csrfToken}">
                            <input type="hidden" name="id" value="${g.id}">
                            <button type="submit" class="btn-delete">Hapus</button>
                        </form>
                    </div>
                </article>
            </c:forEach>
        </div>
    </section>
</div>
</t:layout>
