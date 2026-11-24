<%@ tag language="java" pageEncoding="UTF-8" body-content="empty" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ attribute name="action" required="true" %>
<%@ attribute name="label" required="true" %>
<%@ attribute name="idVal" required="false" %>
<%@ attribute name="jenisVal" required="false" %>
<%@ attribute name="dompetVal" required="false" %>
<%@ attribute name="kategoriVal" required="false" %>
<%@ attribute name="judulVal" required="false" %>
<%@ attribute name="nominalVal" required="false" %>
<%@ attribute name="tanggalVal" required="false" %>
<%@ attribute name="catatanVal" required="false" %>
<%@ attribute name="kategoris" required="true" type="java.lang.Object" %>
<%@ attribute name="dompets" required="true" type="java.lang.Object" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:if test="${empty dompets}">
    <div class="form-hint-warning">Kamu belum punya dompet. <a href="${ctx}/dompet">Buat dompet</a> terlebih dahulu.</div>
</c:if>
<form action="${action}" method="POST">
    <input type="hidden" name="_csrf" value="${csrfToken}">
    <c:if test="${not empty idVal}"><input type="hidden" name="id" value="${idVal}"></c:if>
    <div class="transaction-form-grid">
        <div class="form-group">
            <label for="jenis">Jenis Transaksi</label>
            <select id="jenis" name="jenis" required>
                <option value="">Pilih jenis transaksi</option>
                <option value="pemasukan" ${jenisVal == 'pemasukan' ? 'selected' : ''}>Pemasukan</option>
                <option value="pengeluaran" ${jenisVal == 'pengeluaran' ? 'selected' : ''}>Pengeluaran</option>
            </select>
        </div>
        <div class="form-group">
            <label for="judul">Nama Transaksi</label>
            <input type="text" id="judul" name="judul" value="<c:out value='${judulVal}'/>" placeholder="Contoh: Gaji bulanan" maxlength="255" required>
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
            <small id="kategori-hint">Pilih jenis transaksi terlebih dahulu.</small>
        </div>
        <div class="form-group">
            <label for="nominal">Nominal (Rp)</label>
            <input type="number" id="nominal" name="nominal" value="<c:out value='${nominalVal}'/>" placeholder="Contoh: 50000" min="0.01" step="0.01" required>
        </div>
        <div class="form-group">
            <label for="tanggal">Tanggal</label>
            <input type="date" id="tanggal" name="tanggal" value="<c:out value='${tanggalVal}'/>" required>
        </div>
        <div class="form-group">
            <label for="catatan">Catatan (Opsional)</label>
            <textarea id="catatan" name="catatan" rows="2" placeholder="Tambahkan catatan transaksi..."><c:out value="${catatanVal}"/></textarea>
        </div>
    </div>
    <button type="submit" class="btn-primary">${label}</button>
</form>
<script src="${ctx}/assets/js/kategori-by-jenis.js"></script>
