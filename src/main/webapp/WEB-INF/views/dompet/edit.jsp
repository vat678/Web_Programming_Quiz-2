<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:set var="posted" value="${not empty errors}"/>
<c:set var="namaVal" value="${posted ? param.nama : dompet.nama}"/>
<c:set var="tipeVal" value="${posted ? param.tipe : dompet.tipe}"/>
<c:set var="saldoVal" value="${posted ? param.saldo_awal : dompet.saldoAwal}"/>
<t:layout title="Edit Dompet" heading="Edit Dompet" description="Ubah nama, tipe, atau saldo awal dompet." active="dompet">
<div class="transactions-page">
    <section class="content-card">
        <h2>Edit Dompet</h2>
        <form action="${ctx}/dompet/update" method="POST">
            <input type="hidden" name="_csrf" value="${csrfToken}">
            <input type="hidden" name="id" value="${dompet.id}">
            <div class="form-grid">
                <div class="form-group">
                    <label for="nama">Nama Dompet</label>
                    <input type="text" id="nama" name="nama" value="<c:out value='${namaVal}'/>" maxlength="100" required>
                </div>
                <div class="form-group">
                    <label for="tipe">Tipe</label>
                    <select id="tipe" name="tipe" required>
                        <option value="tunai" ${tipeVal == 'tunai' ? 'selected' : ''}>Tunai</option>
                        <option value="bank" ${tipeVal == 'bank' ? 'selected' : ''}>Bank</option>
                        <option value="ewallet" ${tipeVal == 'ewallet' ? 'selected' : ''}>E-Wallet</option>
                    </select>
                </div>
                <div class="form-group">
                    <label for="saldo_awal">Saldo Awal (Rp)</label>
                    <input type="number" id="saldo_awal" name="saldo_awal" min="0" step="0.01" value="<c:out value='${saldoVal}'/>" required>
                </div>
            </div>
            <div class="transaction-actions">
                <button type="submit" class="btn-primary">Simpan Perubahan</button>
                <a href="${ctx}/dompet" class="btn-edit">Batal</a>
            </div>
        </form>
    </section>
</div>
</t:layout>
