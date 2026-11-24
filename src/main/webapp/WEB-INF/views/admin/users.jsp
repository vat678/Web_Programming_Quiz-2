<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
    <%@ taglib prefix="c" uri="jakarta.tags.core" %>
        <%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
            <c:set var="ctx" value="${pageContext.request.contextPath}" />
            <t:layout title="Kelola Pengguna" heading="Kelola Pengguna"
                description="Daftar akun yang terdaftar di MoneyTrack (khusus admin)." active="admin">
                <div class="transactions-page">
                    <section class="content-card">
                        <div class="section-header">
                            <div>
                                <h2>Daftar Pengguna</h2>
                                <p class="section-description">Cari akun, ubah data dan peran, reset password, atau
                                    hapus pengguna.</p>
                            </div>
                            <span class="item-count">${daftarUser.size()} pengguna ditampilkan</span>
                        </div>

                        <div class="summary-chips">
                            <div class="summary-chip">Administrator<strong>${totalAdmin}</strong></div>
                            <div class="summary-chip">Pengguna biasa<strong>${totalUser}</strong></div>
                        </div>

                        <form method="GET" action="${ctx}/admin/users" class="filter-bar">
                            <div class="form-group">
                                <label for="f-q">Cari</label>
                                <input type="text" id="f-q" name="q" value="<c:out value='${q}'/>"
                                    placeholder="Nama atau email">
                            </div>
                            <div class="form-group">
                                <label for="f-role">Peran</label>
                                <select id="f-role" name="role">
                                    <option value="">Semua</option>
                                    <option value="admin" ${roleFilter=='admin' ? 'selected' : '' }>Admin</option>
                                    <option value="user" ${roleFilter=='user' ? 'selected' : '' }>Pengguna</option>
                                </select>
                            </div>
                            <div class="filter-actions">
                                <button type="submit" class="btn-primary">Terapkan</button>
                                <c:if test="${filterAktif}"><a href="${ctx}/admin/users" class="btn-edit">Reset</a>
                                </c:if>
                            </div>
                        </form>

                        <div class="table-wrapper">
                            <table class="data-table">
                                <thead>
                                    <tr>
                                        <th>PENGGUNA</th>
                                        <th>PERAN</th>
                                        <th>DOMPET</th>
                                        <th>TRANSAKSI</th>
                                        <th>BERGABUNG</th>
                                        <th>AKSI</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:forEach items="${daftarUser}" var="u">
                                        <c:set var="diriSendiri" value="${u.id == sessionScope.user.id}" />
                                        <tr>
                                            <td>
                                                <strong>
                                                    <c:out value="${u.nama}" />
                                                </strong>
                                                <c:if test="${diriSendiri}"> <span
                                                        class="goal-badge goal-tercapai">Kamu</span></c:if>
                                                <br><span class="section-description">
                                                    <c:out value="${u.email}" />
                                                </span>
                                            </td>
                                            <td><span
                                                    class="goal-badge ${u.admin ? 'goal-terlambat' : 'goal-berjalan'}">${u.admin
                                                    ? 'Admin' : 'Pengguna'}</span></td>
                                            <td>${u.jumlahDompet}</td>
                                            <td>${u.jumlahTransaksi}</td>
                                            <td>${u.createdAtFormatted}</td>
                                            <td>
                                                <div class="transaction-actions">
                                                    <a href="${ctx}/admin/users/edit?id=${u.id}"
                                                        class="btn-edit">Edit</a>
                                                    <c:if test="${not diriSendiri}">
                                                        <form action="${ctx}/admin/users/delete" method="POST"
                                                            style="display:inline"
                                                            onsubmit="return confirm('Hapus pengguna ini beserta SEMUA datanya (dompet, transaksi, anggaran, dll)? Tindakan ini tidak dapat dibatalkan.')">
                                                            <input type="hidden" name="_csrf" value="${csrfToken}">
                                                            <input type="hidden" name="id" value="${u.id}">
                                                            <button type="submit" class="btn-delete">Hapus</button>
                                                        </form>
                                                    </c:if>
                                                </div>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                    <c:if test="${empty daftarUser}">
                                        <tr>
                                            <td colspan="6" class="empty-state">
                                                <c:choose>
                                                    <c:when test="${filterAktif}">Tidak ada pengguna yang cocok dengan
                                                        pencarian.</c:when>
                                                    <c:otherwise>Belum ada pengguna.</c:otherwise>
                                                </c:choose>
                                            </td>
                                        </tr>
                                    </c:if>
                                </tbody>
                            </table>
                        </div>
                    </section>
                </div>
            </t:layout>