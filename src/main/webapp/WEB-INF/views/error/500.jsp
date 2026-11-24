<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" isErrorPage="true" %>
<!DOCTYPE html>
<html lang="id"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1">
<title>500 | MoneyTrack</title>
<link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/auth.css">
</head><body><main class="auth-card" style="text-align:center">
<div class="brand">MoneyTrack</div><h1 style="font-size:56px;margin:16px 0 8px">500</h1>
<h2 style="margin:0 0 8px">Terjadi Kesalahan</h2><p class="muted">Ada masalah di server. Silakan coba lagi nanti.</p>
<div class="foot"><a href="<%= request.getContextPath() %>/">Kembali ke beranda</a></div>
</main></body></html>
