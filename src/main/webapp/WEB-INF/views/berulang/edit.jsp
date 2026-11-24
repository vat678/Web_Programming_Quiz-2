<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:set var="posted" value="${not empty errors}"/>
<c:set var="jenisVal" value="${posted ? param.jenis : item.jenis}"/>
<c:set var="judulVal" value="${posted ? param.judul : item.judul}"/>
<c:set var="dompetVal" value="${posted ? param.dompet_id : item.dompetId}"/>
<c:set var="kategoriVal" value="${posted ? param.kategori_id : item.kategoriId}"/>
<c:set var="nominalVal" value="${posted ? param.nominal : item.nominal.toPlainString()}"/>
<c:set var="frekVal" value="${posted ? param.frekuensi : item.frekuensi}"/>
<c:set var="tglVal" value="${posted ? param.tanggal_berikutnya : item.tanggalBerikutnyaIso}"/>
<c:set var="aktifVal" value="${posted ? param.aktif : (item.aktif ? '1' : '0')}"/>
<t:layout title="Edit Transaksi Berulang" heading="Edit Transaksi Berulang" description="Ubah detail transaksi rutin." active="berulang">
<div class="transactions-page">
    <section class="content-card">
        <h2>Edit Transaksi Berulang</h2>
        <form action="${ctx}/berulang/update" method="POST">
            <input type="hidden" name="_csrf" value="${csrfToken}">
            <input type="hidden" name="id" value="${item.id}">
            <div class="transaction-form-grid">
                <div class="form-group">
                    <label for="jenis">Jenis Transaksi</label>
                    <select id="jenis" name="jenis" required>
                        <option value="pemasukan" ${jenisVal == 'pemasukan' ? 'selected' : ''}>Pemasukan</option>
                        <option value="pengeluaran" ${jenisVal == 'pengeluaran' ? 'selected' : ''}>Pengeluaran</option>
                    </select>
                </div>
                <div class="form-group">
                    <label for="judul">Nama Transaksi</label>
                    <input type="text" id="judul" name="judul" maxlength="255" value="<c:out value='${judulVal}'/>" required>
                </div>
                <div class="form-group">
                    <label for="dompet_id">Dompet</label>
                    <select id="dompet_id" name="dompet_id" required>
                        <c:forEach items="${dompets}" var="d">
                            <option value="${d.id}" ${dompetVal == d.id ? 'selected' : ''}><c:out value="${d.nama}"/></option>
                        </c:forEach>
                    </select>
                </div>
                <div class="form-group">
                    <label for="kategori_id">Kategori</label>
                    <select id="kategori_id" name="kategori_id">
                        <option value="">Tanpa kategori</option>
                        <c:forEach items="${kategoris}" var="k">
                            <option value="${k.id}" data-jenis="${k.jenis}" ${kategoriVal == k.id ? 'selected' : ''}>
                                <c:out value="${k.nama}"/> (${k.jenis == 'pemasukan' ? 'Pemasukan' : 'Pengeluaran'})
                            </option>
                        </c:forEach>
                    </select>
                    <small id="kategori-hint">Kategori ditampilkan berdasarkan jenis transaksi.</small>
                </div>
                <div class="form-group">
                    <label for="nominal">Nominal (Rp)</label>
                    <input type="number" id="nominal" name="nominal" min="0.01" step="0.01" value="<c:out value='${nominalVal}'/>" required>
                </div>
                <div class="form-group">
                    <label for="frekuensi">Frekuensi</label>
                    <select id="frekuensi" name="frekuensi" required>
                        <option value="harian" ${frekVal == 'harian' ? 'selected' : ''}>Harian</option>
                        <option value="mingguan" ${frekVal == 'mingguan' ? 'selected' : ''}>Mingguan</option>
                        <option value="bulanan" ${frekVal == 'bulanan' ? 'selected' : ''}>Bulanan</option>
                        <option value="tahunan" ${frekVal == 'tahunan' ? 'selected' : ''}>Tahunan</option>
                    </select>
                </div>
                <div class="form-group">
                    <label for="tanggal_berikutnya">Tanggal Berikutnya</label>
                    <input type="date" id="tanggal_berikutnya" name="tanggal_berikutnya" value="<c:out value='${tglVal}'/>" required>
                </div>
                <div class="form-group">
                    <label for="aktif">Status</label>
                    <select id="aktif" name="aktif">
                        <option value="1" ${aktifVal == '1' ? 'selected' : ''}>Aktif</option>
                        <option value="0" ${aktifVal == '0' ? 'selected' : ''}>Dijeda</option>
                    </select>
                </div>
            </div>
            <div class="transaction-actions">
                <button type="submit" class="btn-primary">Simpan Perubahan</button>
                <a href="${ctx}/berulang" class="btn-edit">Batal</a>
            </div>
        </form>
    </section>
</div>
<script src="${ctx}/assets/js/kategori-by-jenis.js"></script>
</t:layout>
