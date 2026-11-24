<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:set var="flashOk" value="${sessionScope.flash_success}"/>
<c:remove var="flash_success" scope="session"/>
<!DOCTYPE html>
<html lang="id"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1">
<title>MoneyTrack | Masuk</title>
<link rel="stylesheet" href="${ctx}/assets/css/auth.css">
</head><body><main class="auth-card">
<div class="brand">MoneyTrack</div><h1>Masuk</h1><p class="muted">Catat dan kelola keuangan pribadimu.</p>
<c:if test="${not empty flashOk}"><div class="success"><c:out value="${flashOk}"/></div></c:if>
<c:if test="${not empty errors}"><div class="error"><ul style="margin:0;padding-left:20px"><c:forEach items="${errors}" var="e"><li><c:out value="${e.value}"/></li></c:forEach></ul></div></c:if>
<form method="POST" action="${ctx}/login">
<input type="hidden" name="_csrf" value="${csrfToken}">
<label for="email">Email</label><input id="email" type="email" name="email" value="<c:out value='${param.email}'/>" autocomplete="email" required autofocus>
<label for="password">Password</label><input id="password" type="password" name="password" autocomplete="current-password" required>
<button type="submit">Masuk</button></form>
<div class="foot">Belum punya akun? <a href="${ctx}/register">Daftar</a></div>
</main></body></html>
