<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:set var="posted" value="${not empty errors}"/>
<c:set var="jenisVal" value="${posted ? param.jenis : hutang.jenis}"/>
<c:set var="pihakVal" value="${posted ? param.nama_pihak : hutang.namaPihak}"/>
<c:set var="nominalVal" value="${posted ? param.nominal : hutang.nominal.toPlainString()}"/>
<c:set var="terbayarVal" value="${posted ? param.terbayar : hutang.terbayar.toPlainString()}"/>
<c:set var="tempoVal" value="${posted ? param.jatuh_tempo : hutang.jatuhTempoIso}"/>
<c:set var="catatanVal" value="${posted ? param.catatan : hutang.catatan}"/>
<t:layout title="Edit Hutang / Piutang" heading="Edit Hutang / Piutang" description="Ubah detail catatan atau koreksi jumlah terbayar." active="hutang">
<div class="transactions-page">
    <section class="content-card">
        <h2>Edit Catatan</h2>
        <form action="${ctx}/hutang/update" method="POST">
            <input type="hidden" name="_csrf" value="${csrfToken}">
            <input type="hidden" name="id" value="${hutang.id}">
            <div class="form-grid">
                <div class="form-group">
                    <label for="jenis">Jenis</label>
                    <select id="jenis" name="jenis" required>
                        <option value="hutang" ${jenisVal == 'hutang' ? 'selected' : ''}>Hutang (saya berutang)</option>
                        <option value="piutang" ${jenisVal == 'piutang' ? 'selected' : ''}>Piutang (orang berutang ke saya)</option>
                    </select>
                </div>
                <div class="form-group">
                    <label for="nama_pihak">Nama Pihak</label>
                    <input type="text" id="nama_pihak" name="nama_pihak" maxlength="150" value="<c:out value='${pihakVal}'/>" required>
                </div>
                <div class="form-group">
                    <label for="nominal">Nominal (Rp)</label>
                    <input type="number" id="nominal" name="nominal" min="1" step="0.01" value="<c:out value='${nominalVal}'/>" required>
                </div>
                <div class="form-group">
                    <label for="terbayar">Sudah Dibayar (Rp)</label>
                    <input type="number" id="terbayar" name="terbayar" min="0" step="0.01" value="<c:out value='${terbayarVal}'/>" required>
                </div>
                <div class="form-group">
                    <label for="jatuh_tempo">Jatuh Tempo (opsional)</label>
                    <input type="date" id="jatuh_tempo" name="jatuh_tempo" value="<c:out value='${tempoVal}'/>">
                </div>
            </div>
            <div class="form-group">
                <label for="catatan">Catatan (opsional)</label>
                <textarea id="catatan" name="catatan" rows="3" maxlength="5000"><c:out value="${catatanVal}"/></textarea>
            </div>
            <div class="transaction-actions">
                <button type="submit" class="btn-primary">Simpan Perubahan</button>
                <a href="${ctx}/hutang" class="btn-edit">Batal</a>
            </div>
        </form>
    </section>
</div>
</t:layout>
