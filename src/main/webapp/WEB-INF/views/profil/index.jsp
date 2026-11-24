<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:set var="namaVal" value="${empty param.nama ? profil.nama : param.nama}"/>
<c:set var="emailVal" value="${empty param.email ? profil.email : param.email}"/>
<t:layout title="Profil" heading="Profil" description="Kelola informasi akun dan keamananmu." active="profil">
<div class="transactions-page">

    <section class="card profile-hero">
        <div class="profile-avatar"><c:out value="${sessionScope.user.inisial}"/></div>
        <div class="profile-hero-info">
            <h2><c:out value="${profil.nama}"/></h2>
            <p class="section-description"><c:out value="${profil.email}"/></p>
            <p class="section-description">
                <span class="goal-badge ${profil.admin ? 'goal-terlambat' : 'goal-berjalan'}">${profil.admin ? 'Administrator' : 'Pengguna'}</span>
                &nbsp; Bergabung sejak ${profil.createdAtFormatted}
            </p>
        </div>
        <div class="profile-stats">
            <div><strong>${jumlahDompet}</strong><span>Dompet</span></div>
            <div><strong>${jumlahKategori}</strong><span>Kategori</span></div>
            <div><strong>${jumlahTransaksi}</strong><span>Transaksi</span></div>
        </div>
    </section>

    <section class="content-card">
        <h2>Informasi Akun</h2>
        <p class="section-description">Nama dan email ini tampil di header aplikasi dan dipakai untuk login.</p>
        <form action="${ctx}/profil/update" method="POST">
            <input type="hidden" name="_csrf" value="${csrfToken}">
            <div class="form-grid">
                <div class="form-group">
                    <label for="nama">Nama</label>
                    <input type="text" id="nama" name="nama" maxlength="255" value="<c:out value='${namaVal}'/>" autocomplete="name" required>
                </div>
                <div class="form-group">
                    <label for="email">Email</label>
                    <input type="email" id="email" name="email" maxlength="255" value="<c:out value='${emailVal}'/>" autocomplete="email" required>
                </div>
            </div>
            <button type="submit" class="btn-primary">Simpan Perubahan</button>
        </form>
    </section>

    <section class="content-card">
        <h2>Ganti Password</h2>
        <p class="section-description">Gunakan password minimal 8 karakter yang berbeda dari sebelumnya.</p>
        <form action="${ctx}/profil/password" method="POST">
            <input type="hidden" name="_csrf" value="${csrfToken}">
            <div class="form-grid">
                <div class="form-group">
                    <label for="password_lama">Password Saat Ini</label>
                    <input type="password" id="password_lama" name="password_lama" autocomplete="current-password" required>
                </div>
                <div class="form-group">
                    <label for="password_baru">Password Baru</label>
                    <input type="password" id="password_baru" name="password_baru" minlength="8" maxlength="72" autocomplete="new-password" required>
                </div>
                <div class="form-group">
                    <label for="password_konfirmasi">Konfirmasi Password Baru</label>
                    <input type="password" id="password_konfirmasi" name="password_konfirmasi" minlength="8" maxlength="72" autocomplete="new-password" required>
                </div>
            </div>
            <button type="submit" class="btn-primary">Ganti Password</button>
        </form>
    </section>
</div>
</t:layout>
