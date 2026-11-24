<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:set var="namaVal" value="${empty param.nama ? kategori.nama : param.nama}"/>
<c:set var="jenisVal" value="${empty param.jenis ? kategori.jenis : param.jenis}"/>
<t:layout title="Edit Kategori" heading="Edit Kategori" description="Ubah nama atau jenis kategori." active="kategori">
<div class="transactions-page">
    <section class="content-card">
        <h2>Edit Kategori</h2>
        <c:if test="${kategori.dipakai}">
            <p class="section-description">Kategori ini sudah dipakai, jadi jenisnya tidak dapat diubah.</p>
        </c:if>
        <form action="${ctx}/kategori/update" method="POST">
            <input type="hidden" name="_csrf" value="${csrfToken}">
            <input type="hidden" name="id" value="${kategori.id}">
            <div class="form-grid">
                <div class="form-group">
                    <label for="nama">Nama Kategori</label>
                    <input type="text" id="nama" name="nama" value="<c:out value='${namaVal}'/>" maxlength="100" required>
                </div>
                <div class="form-group">
                    <label for="jenis">Jenis Kategori</label>
                    <select id="jenis" name="jenis" required>
                        <option value="pemasukan" ${jenisVal == 'pemasukan' ? 'selected' : ''}>Pemasukan</option>
                        <option value="pengeluaran" ${jenisVal == 'pengeluaran' ? 'selected' : ''}>Pengeluaran</option>
                    </select>
                </div>
            </div>
            <div class="transaction-actions">
                <button type="submit" class="btn-primary">Simpan Perubahan</button>
                <a href="${ctx}/kategori" class="btn-edit">Batal</a>
            </div>
        </form>
    </section>
</div>
</t:layout>
