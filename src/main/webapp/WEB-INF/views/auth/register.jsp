<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<!DOCTYPE html>
<html lang="id"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1">
<title>MoneyTrack | Buat Akun</title>
<link rel="stylesheet" href="${ctx}/assets/css/auth.css">
</head><body><main class="auth-card">
<div class="brand">MoneyTrack</div><h1>Buat Akun</h1><p class="muted">Catat dan kelola keuangan pribadimu.</p>
<c:if test="${not empty errors}"><div class="error"><ul style="margin:0;padding-left:20px"><c:forEach items="${errors}" var="e"><li><c:out value="${e.value}"/></li></c:forEach></ul></div></c:if>
<form method="POST" action="${ctx}/register">
<input type="hidden" name="_csrf" value="${csrfToken}">
<label for="nama">Nama</label><input id="nama" type="text" name="nama" value="<c:out value='${param.nama}'/>" autocomplete="name" required autofocus>
<label for="email">Email</label><input id="email" type="email" name="email" value="<c:out value='${param.email}'/>" autocomplete="email" required>
<label for="password">Password (minimal 8 karakter)</label><input id="password" type="password" name="password" autocomplete="new-password" required>
<label for="password_confirmation">Konfirmasi password</label><input id="password_confirmation" type="password" name="password_confirmation" autocomplete="new-password" required>
<button type="submit">Buat Akun</button></form>
<div class="foot">Sudah punya akun? <a href="${ctx}/login">Masuk</a></div>
</main></body></html>
