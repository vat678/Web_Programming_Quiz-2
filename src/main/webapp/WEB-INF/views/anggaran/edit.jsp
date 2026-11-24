<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:set var="posted" value="${not empty errors}"/>
<c:set var="kategoriVal" value="${posted ? param.kategori_id : anggaran.kategoriId}"/>
<c:set var="jumlahVal" value="${posted ? param.jumlah_anggaran : anggaran.jumlah.toPlainString()}"/>
<c:set var="bulanVal" value="${posted ? param.bulan : anggaran.bulan}"/>
<c:set var="tahunVal" value="${posted ? param.tahun : anggaran.tahun}"/>
<t:layout title="Edit Anggaran" heading="Edit Anggaran" description="Ubah kategori, jumlah, atau periode anggaran." active="anggaran">
<div class="budget-page">
    <section class="budget-card">
        <div class="budget-card-heading">
            <div>
                <h2>Edit Anggaran</h2>
                <p>Terpakai saat ini: Rp <c:out value="${anggaran.terpakai.toPlainString()}"/> (${anggaran.persen}% dari anggaran).</p>
            </div>
        </div>
        <form action="${ctx}/anggaran/update" method="POST">
            <input type="hidden" name="_csrf" value="${csrfToken}">
            <input type="hidden" name="id" value="${anggaran.id}">
            <div class="budget-form-grid">
                <div class="budget-form-group">
                    <label for="kategori_id">Kategori Pengeluaran</label>
                    <select id="kategori_id" name="kategori_id" required>
                        <c:forEach items="${kategoris}" var="k">
                            <option value="${k.id}" ${kategoriVal == k.id ? 'selected' : ''}><c:out value="${k.nama}"/></option>
                        </c:forEach>
                    </select>
                </div>
                <div class="budget-form-group">
                    <label for="jumlah_anggaran">Jumlah Anggaran (Rp)</label>
                    <input type="number" id="jumlah_anggaran" name="jumlah_anggaran" min="1" step="0.01"
                           value="<c:out value='${jumlahVal}'/>" required>
                </div>
                <div class="budget-form-group">
                    <label for="bulan">Bulan</label>
                    <select id="bulan" name="bulan" required>
                        <c:forEach items="${namaBulan}" var="nb" varStatus="st">
                            <option value="${st.count}" ${st.count == bulanVal ? 'selected' : ''}>${nb}</option>
                        </c:forEach>
                    </select>
                </div>
                <div class="budget-form-group">
                    <label for="tahun">Tahun</label>
                    <input type="number" id="tahun" name="tahun" min="2000" max="2100" value="<c:out value='${tahunVal}'/>" required>
                </div>
            </div>
            <div class="budget-edit-actions">
                <button type="submit" class="budget-button">Simpan Perubahan</button>
                <a href="${ctx}/anggaran?bulan=${anggaran.bulan}&tahun=${anggaran.tahun}" class="budget-cancel-button">Batal</a>
            </div>
        </form>
    </section>
</div>
</t:layout>
