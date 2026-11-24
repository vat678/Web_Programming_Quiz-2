<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:set var="posted" value="${not empty errors}"/>
<t:layout title="Edit Transaksi" heading="Edit Transaksi" description="Perbarui data transaksi." active="transaksi">
<section class="transactions-page">
    <div class="card transaction-form-card">
        <h2 class="section-title">Edit Transaksi</h2>
        <t:transaksi-form action="${ctx}/transaksi/update" label="Simpan Perubahan" idVal="${transaksi.id}"
            jenisVal="${posted ? param.jenis : transaksi.jenis}" dompetVal="${posted ? param.dompet_id : transaksi.dompetId}"
            kategoriVal="${posted ? param.kategori_id : transaksi.kategoriId}" judulVal="${posted ? param.judul : transaksi.judul}"
            nominalVal="${posted ? param.nominal : transaksi.nominal}" tanggalVal="${posted ? param.tanggal : transaksi.tanggalIso}"
            catatanVal="${posted ? param.catatan : transaksi.catatan}" kategoris="${kategoris}" dompets="${dompets}"/>
        <p style="margin-top:14px"><a href="${ctx}/transaksi" class="btn-edit">← Kembali ke daftar</a></p>
    </div>
</section>
</t:layout>
