<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
    <%@ taglib prefix="c" uri="jakarta.tags.core" %>
        <%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
            <c:set var="ctx" value="${pageContext.request.contextPath}" />
            <c:set var="namaVal" value="${empty param.nama ? target.nama : param.nama}" />
            <c:set var="emailVal" value="${empty param.email ? target.email : param.email}" />
            <c:set var="roleVal" value="${empty param.role ? target.role : param.role}" />
            <t:layout title="Edit Pengguna" heading="Edit Pengguna"
                description="Ubah data akun, reset password, atau hapus pengguna." active="admin">
                <div class="transactions-page">

                    <section class="content-card">
                        <h2>Data Akun</h2>
                        <p class="section-description">Bergabung sejak ${target.createdAtFormatted}.</p>
                        <c:if test="${diriSendiri}">
                            <div class="form-hint-warning">Ini akunmu sendiri. Peran tidak dapat diubah dan akun tidak
                                dapat dihapus dari halaman ini.</div>
                        </c:if>
                        <form action="${ctx}/admin/users/update" method="POST">
                            <input type="hidden" name="_csrf" value="${csrfToken}">
                            <input type="hidden" name="id" value="${target.id}">
                            <div class="form-grid">
                                <div class="form-group">
                                    <label for="nama">Nama</label>
                                    <input type="text" id="nama" name="nama" maxlength="255"
                                        value="<c:out value='${namaVal}'/>" required>
                                </div>
                                <div class="form-group">
                                    <label for="email">Email</label>
                                    <input type="email" id="email" name="email" maxlength="255"
                                        value="<c:out value='${emailVal}'/>" required>
                                </div>
                                <div class="form-group">
                                    <label for="role">Peran</label>
                                    <c:choose>
                                        <c:when test="${diriSendiri}">
                                            <input type="text" id="role" value="${target.admin ? 'Admin' : 'Pengguna'}"
                                                disabled>
                                            <input type="hidden" name="role" value="${target.role}">
                                        </c:when>
                                        <c:otherwise>
                                            <select id="role" name="role" required>
                                                <option value="user" ${roleVal=='user' ? 'selected' : '' }>Pengguna
                                                </option>
                                                <option value="admin" ${roleVal=='admin' ? 'selected' : '' }>Admin
                                                </option>
                                            </select>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                            </div>
                            <div class="transaction-actions">
                                <button type="submit" class="btn-primary">Simpan Perubahan</button>
                                <a href="${ctx}/admin/users" class="btn-edit">Batal</a>
                            </div>
                        </form>
                    </section>

                    <section class="content-card">
                        <h2>Reset Password</h2>
                        <p class="section-description">Tetapkan password baru untuk pengguna ini (minimal 8 karakter).
                            Sampaikan password baru kepada pemiliknya.</p>
                        <form action="${ctx}/admin/users/password" method="POST">
                            <input type="hidden" name="_csrf" value="${csrfToken}">
                            <input type="hidden" name="id" value="${target.id}">
                            <div class="form-grid">
                                <div class="form-group">
                                    <label for="password_baru">Password Baru</label>
                                    <input type="password" id="password_baru" name="password_baru" minlength="8"
                                        maxlength="72" autocomplete="new-password" required>
                                </div>
                                <div class="form-group">
                                    <label for="password_konfirmasi">Konfirmasi Password Baru</label>
                                    <input type="password" id="password_konfirmasi" name="password_konfirmasi"
                                        minlength="8" maxlength="72" autocomplete="new-password" required>
                                </div>
                            </div>
                            <button type="submit" class="btn-primary">Reset Password</button>
                        </form>
                    </section>

                    <c:if test="${not diriSendiri}">
                        <section class="content-card">
                            <h2>Hapus Pengguna</h2>
                            <p class="section-description">Menghapus akun ini juga menghapus seluruh dompet, kategori,
                                transaksi, anggaran, target tabungan, dan catatan hutang/piutang miliknya. Tindakan ini
                                tidak dapat dibatalkan.</p>
                            <form action="${ctx}/admin/users/delete" method="POST"
                                onsubmit="return confirm('Hapus pengguna ini beserta SEMUA datanya? Tindakan ini tidak dapat dibatalkan.')">
                                <input type="hidden" name="_csrf" value="${csrfToken}">
                                <input type="hidden" name="id" value="${target.id}">
                                <button type="submit" class="btn-delete">Hapus Pengguna</button>
                            </form>
                        </section>
                    </c:if>
                </div>
            </t:layout>