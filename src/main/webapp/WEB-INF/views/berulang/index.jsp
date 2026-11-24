<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<t:layout title="Transaksi Berulang" heading="Transaksi Berulang" description="Otomatiskan pencatatan gaji, tagihan, dan langganan rutin." active="berulang">
<div class="transactions-page">

    <section class="summary-cards">
        <div class="card">
            <div class="card-top"><span class="card-label">Template Aktif</span><div class="card-icon">↻</div></div>
            <h2>${jumlahAktif} / ${items.size()}</h2>
            <p class="card-description">Transaksi rutin yang sedang berjalan.</p>
        </div>
        <div class="card">
            <div class="card-top"><span class="card-label">Jatuh Tempo</span><div class="card-icon expense-icon">!</div></div>
            <h2>${jumlahJatuhTempo}</h2>
            <p class="card-description">Siap diproses menjadi transaksi.</p>
        </div>
        <div class="card">
            <div class="card-top"><span class="card-label">Proses Sekarang</span><div class="card-icon income-icon">▶</div></div>
            <form action="${ctx}/berulang/proses" method="POST">
                <input type="hidden" name="_csrf" value="${csrfToken}">
                <button type="submit" class="btn-primary" ${jumlahJatuhTempo == 0 ? 'disabled' : ''}>Buat Transaksi Jatuh Tempo</button>
            </form>
            <p class="card-description" style="margin-top:12px">Periode yang terlewat ikut dibuat dengan tanggalnya masing-masing.</p>
        </div>
    </section>

    <section class="content-card">
        <h2>Tambah Transaksi Berulang</h2>
        <p class="section-description">Contoh: Gaji bulanan, Tagihan listrik, Langganan streaming.</p>
        <c:if test="${empty dompets}">
            <div class="form-hint-warning">Kamu belum punya dompet. <a href="${ctx}/dompet">Buat dompet</a> terlebih dahulu.</div>
        </c:if>
        <form action="${ctx}/berulang/create" method="POST">
            <input type="hidden" name="_csrf" value="${csrfToken}">
            <div class="transaction-form-grid">
                <div class="form-group">
                    <label for="jenis">Jenis Transaksi</label>
                    <select id="jenis" name="jenis" required>
                        <option value="">Pilih jenis transaksi</option>
                        <option value="pemasukan" ${param.jenis == 'pemasukan' ? 'selected' : ''}>Pemasukan</option>
                        <option value="pengeluaran" ${param.jenis == 'pengeluaran' ? 'selected' : ''}>Pengeluaran</option>
                    </select>
                </div>
                <div class="form-group">
                    <label for="judul">Nama Transaksi</label>
                    <input type="text" id="judul" name="judul" maxlength="255" placeholder="Contoh: Gaji bulanan"
                           value="<c:out value='${param.judul}'/>" required>
                </div>
                <div class="form-group">
                    <label for="dompet_id">Dompet</label>
                    <select id="dompet_id" name="dompet_id" required>
                        <c:forEach items="${dompets}" var="d">
                            <option value="${d.id}" ${param.dompet_id == d.id ? 'selected' : ''}><c:out value="${d.nama}"/></option>
                        </c:forEach>
                    </select>
                </div>
                <div class="form-group">
                    <label for="kategori_id">Kategori</label>
                    <select id="kategori_id" name="kategori_id">
                        <option value="">Tanpa kategori</option>
                        <c:forEach items="${kategoris}" var="k">
                            <option value="${k.id}" data-jenis="${k.jenis}" ${param.kategori_id == k.id ? 'selected' : ''}>
                                <c:out value="${k.nama}"/> (${k.jenis == 'pemasukan' ? 'Pemasukan' : 'Pengeluaran'})
                            </option>
                        </c:forEach>
                    </select>
                    <small id="kategori-hint">Pilih jenis transaksi terlebih dahulu.</small>
                </div>
                <div class="form-group">
                    <label for="nominal">Nominal (Rp)</label>
                    <input type="number" id="nominal" name="nominal" min="0.01" step="0.01" placeholder="Contoh: 50000"
                           value="<c:out value='${param.nominal}'/>" required>
                </div>
                <div class="form-group">
                    <label for="frekuensi">Frekuensi</label>
                    <select id="frekuensi" name="frekuensi" required>
                        <option value="harian" ${param.frekuensi == 'harian' ? 'selected' : ''}>Harian</option>
                        <option value="mingguan" ${param.frekuensi == 'mingguan' ? 'selected' : ''}>Mingguan</option>
                        <option value="bulanan" ${empty param.frekuensi || param.frekuensi == 'bulanan' ? 'selected' : ''}>Bulanan</option>
                        <option value="tahunan" ${param.frekuensi == 'tahunan' ? 'selected' : ''}>Tahunan</option>
                    </select>
                </div>
                <div class="form-group">
                    <label for="tanggal_berikutnya">Tanggal Pertama / Berikutnya</label>
                    <input type="date" id="tanggal_berikutnya" name="tanggal_berikutnya"
                           value="<c:out value='${empty param.tanggal_berikutnya ? today : param.tanggal_berikutnya}'/>" required>
                </div>
                <div class="form-group">
                    <label for="aktif">Status</label>
                    <select id="aktif" name="aktif">
                        <option value="1" ${empty param.aktif || param.aktif == '1' ? 'selected' : ''}>Aktif</option>
                        <option value="0" ${param.aktif == '0' ? 'selected' : ''}>Dijeda</option>
                    </select>
                </div>
            </div>
            <button type="submit" class="btn-primary">+ Simpan Transaksi Berulang</button>
        </form>
    </section>

    <section class="content-card">
        <div class="section-header">
            <div>
                <h2>Daftar Transaksi Berulang</h2>
                <p class="section-description">Template aktif tampil lebih dulu, diurutkan dari tanggal terdekat.</p>
            </div>
            <span class="item-count">${items.size()} template</span>
        </div>
        <div class="table-wrapper">
            <table class="data-table">
                <thead>
                    <tr><th>TRANSAKSI</th><th>KATEGORI</th><th>DOMPET</th><th>FREKUENSI</th><th>BERIKUTNYA</th><th>STATUS</th><th>NOMINAL</th><th>AKSI</th></tr>
                </thead>
                <tbody>
                <c:forEach items="${items}" var="b">
                    <tr class="${b.aktif ? '' : 'row-paused'}">
                        <td>
                            <strong><c:out value="${b.judul}"/></strong><br>
                            <span class="transaction-type ${b.jenis == 'pemasukan' ? 'badge-income' : 'badge-expense'}">${b.jenis == 'pemasukan' ? 'Pemasukan' : 'Pengeluaran'}</span>
                        </td>
                        <td><c:out value="${empty b.kategoriNama ? 'Tanpa kategori' : b.kategoriNama}"/></td>
                        <td><c:out value="${b.dompetNama}"/></td>
                        <td>${b.frekuensiLabel}</td>
                        <td>${b.tanggalBerikutnyaFormatted}<c:if test="${not empty b.tanggalInfo}"><br><small>${b.tanggalInfo}</small></c:if></td>
                        <td>
                            <c:choose>
                                <c:when test="${not b.aktif}"><span class="goal-badge goal-berjalan">Dijeda</span></c:when>
                                <c:when test="${b.jatuhTempo}"><span class="goal-badge goal-terlambat">Jatuh tempo</span></c:when>
                                <c:otherwise><span class="goal-badge goal-lunas">Aktif</span></c:otherwise>
                            </c:choose>
                        </td>
                        <td class="${b.jenis == 'pemasukan' ? 'amount-income' : 'amount-expense'}">
                            ${b.jenis == 'pemasukan' ? '+' : '-'} Rp <fmt:formatNumber value="${b.nominal}" pattern="#,##0"/>
                        </td>
                        <td>
                            <div class="transaction-actions">
                                <form action="${ctx}/berulang/toggle" method="POST" style="display:inline">
                                    <input type="hidden" name="_csrf" value="${csrfToken}">
                                    <input type="hidden" name="id" value="${b.id}">
                                    <button type="submit" class="btn-edit">${b.aktif ? 'Jeda' : 'Aktifkan'}</button>
                                </form>
                                <a href="${ctx}/berulang/edit?id=${b.id}" class="btn-edit">Edit</a>
                                <form action="${ctx}/berulang/delete" method="POST" style="display:inline"
                                      onsubmit="return confirm('Yakin ingin menghapus transaksi berulang ini? Transaksi yang sudah dibuat tidak ikut terhapus.')">
                                    <input type="hidden" name="_csrf" value="${csrfToken}">
                                    <input type="hidden" name="id" value="${b.id}">
                                    <button type="submit" class="btn-delete">Hapus</button>
                                </form>
                            </div>
                        </td>
                    </tr>
                </c:forEach>
                <c:if test="${empty items}">
                    <tr><td colspan="8" class="empty-state">Belum ada transaksi berulang. Tambahkan lewat formulir di atas.</td></tr>
                </c:if>
                </tbody>
            </table>
        </div>
    </section>
</div>
<script src="${ctx}/assets/js/kategori-by-jenis.js"></script>
</t:layout>
