<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<t:layout title="Kategori" heading="Kategori" description="Kelola kategori pemasukan dan pengeluaranmu." active="kategori">
<div class="transactions-page">
    <section class="content-card">
        <h2>Tambah Kategori</h2>
        <p class="section-description">Buat kategori untuk mengelompokkan transaksi pemasukan dan pengeluaranmu.</p>
        <form action="${ctx}/kategori/create" method="POST">
            <input type="hidden" name="_csrf" value="${csrfToken}">
            <div class="form-grid">
                <div class="form-group">
                    <label for="nama">Nama Kategori</label>
                    <input type="text" id="nama" name="nama" placeholder="Contoh: Makanan" value="<c:out value='${param.nama}'/>" maxlength="100" required>
                </div>
                <div class="form-group">
                    <label for="jenis">Jenis Kategori</label>
                    <select id="jenis" name="jenis" required>
                        <option value="">Pilih jenis kategori</option>
                        <option value="pemasukan" ${param.jenis == 'pemasukan' ? 'selected' : ''}>Pemasukan</option>
                        <option value="pengeluaran" ${param.jenis == 'pengeluaran' ? 'selected' : ''}>Pengeluaran</option>
                    </select>
                </div>
            </div>
            <button type="submit" class="btn-primary">+ Simpan Kategori</button>
        </form>
    </section>

    <section class="content-card">
        <div class="section-header">
            <div>
                <h2>Daftar Kategori</h2>
                <p class="section-description">Kelola kategori yang digunakan dalam transaksi.</p>
            </div>
            <span class="item-count">${kategoris.size()} kategori</span>
        </div>
        <div class="table-wrapper">
            <table class="data-table">
                <thead><tr><th>NAMA KATEGORI</th><th>JENIS</th><th>AKSI</th></tr></thead>
                <tbody>
                <c:forEach items="${kategoris}" var="k">
                    <tr>
                        <td><strong><c:out value="${k.nama}"/></strong></td>
                        <td>
                            <c:choose>
                                <c:when test="${k.jenis == 'pemasukan'}"><span class="badge badge-income">Pemasukan</span></c:when>
                                <c:otherwise><span class="badge badge-expense">Pengeluaran</span></c:otherwise>
                            </c:choose>
                        </td>
                        <td>
                            <div class="transaction-actions">
                                <a href="${ctx}/kategori/edit?id=${k.id}" class="btn-edit">Edit</a>
                                <form action="${ctx}/kategori/delete" method="POST" style="display:inline"
                                      onsubmit="return confirm('Yakin ingin menghapus kategori ini?')">
                                    <input type="hidden" name="_csrf" value="${csrfToken}">
                                    <input type="hidden" name="id" value="${k.id}">
                                    <button type="submit" class="btn-delete">Hapus</button>
                                </form>
                            </div>
                        </td>
                    </tr>
                </c:forEach>
                <c:if test="${empty kategoris}">
                    <tr><td colspan="3" class="empty-state">Belum ada kategori. Tambahkan kategori melalui form di atas.</td></tr>
                </c:if>
                </tbody>
            </table>
        </div>
    </section>
</div>
</t:layout>
