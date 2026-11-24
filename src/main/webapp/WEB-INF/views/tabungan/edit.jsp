<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:set var="posted" value="${not empty errors}"/>
<c:set var="namaVal" value="${posted ? param.nama : target.nama}"/>
<c:set var="targetVal" value="${posted ? param.target_nominal : target.targetNominal.toPlainString()}"/>
<c:set var="terkumpulVal" value="${posted ? param.terkumpul : target.terkumpul.toPlainString()}"/>
<c:set var="tenggatVal" value="${posted ? param.tenggat : target.tenggatIso}"/>
<c:set var="catatanVal" value="${posted ? param.catatan : target.catatan}"/>
<t:layout title="Edit Target Tabungan" heading="Edit Target Tabungan" description="Ubah detail target atau koreksi jumlah terkumpul." active="tabungan">
<div class="transactions-page">
    <section class="content-card">
        <h2>Edit Target</h2>
        <form action="${ctx}/tabungan/update" method="POST">
            <input type="hidden" name="_csrf" value="${csrfToken}">
            <input type="hidden" name="id" value="${target.id}">
            <div class="form-grid">
                <div class="form-group">
                    <label for="nama">Nama Target</label>
                    <input type="text" id="nama" name="nama" maxlength="150" value="<c:out value='${namaVal}'/>" required>
                </div>
                <div class="form-group">
                    <label for="target_nominal">Target Nominal (Rp)</label>
                    <input type="number" id="target_nominal" name="target_nominal" min="1" step="0.01" value="<c:out value='${targetVal}'/>" required>
                </div>
                <div class="form-group">
                    <label for="terkumpul">Sudah Terkumpul (Rp)</label>
                    <input type="number" id="terkumpul" name="terkumpul" min="0" step="0.01" value="<c:out value='${terkumpulVal}'/>" required>
                </div>
                <div class="form-group">
                    <label for="tenggat">Tenggat (opsional)</label>
                    <input type="date" id="tenggat" name="tenggat" value="<c:out value='${tenggatVal}'/>">
                </div>
            </div>
            <div class="form-group">
                <label for="catatan">Catatan (opsional)</label>
                <textarea id="catatan" name="catatan" rows="3" maxlength="5000"><c:out value="${catatanVal}"/></textarea>
            </div>
            <div class="transaction-actions">
                <button type="submit" class="btn-primary">Simpan Perubahan</button>
                <a href="${ctx}/tabungan" class="btn-edit">Batal</a>
            </div>
        </form>
    </section>
</div>
</t:layout>
