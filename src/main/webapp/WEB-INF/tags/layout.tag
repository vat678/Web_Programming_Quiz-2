<%@ tag language="java" pageEncoding="UTF-8" body-content="scriptless" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ attribute name="title" required="true" %>
<%@ attribute name="heading" required="true" %>
<%@ attribute name="description" required="false" %>
<%@ attribute name="active" required="false" %>
<fmt:setLocale value="id_ID" scope="request"/>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:set var="flashOk" value="${sessionScope.flash_success}"/>
<c:set var="flashErr" value="${sessionScope.flash_error}"/>
<c:remove var="flash_success" scope="session"/>
<c:remove var="flash_error" scope="session"/>
<!DOCTYPE html>
<html lang="id">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><c:out value="${title}"/> | MoneyTrack</title>
    <link rel="stylesheet" href="${ctx}/assets/css/app.css">
</head>
<body>
<div class="layout">
    <aside class="sidebar">
        <div class="brand">
            <div class="brand-icon">M</div>
            <div><h2>MoneyTrack</h2><p>Personal Finance</p></div>
        </div>
        <nav class="menu">
            <div class="menu-title">MENU UTAMA</div>
            <a href="${ctx}/" class="${active == 'dashboard' ? 'active' : ''}">▦ &nbsp; Dashboard</a>
            <a href="${ctx}/transaksi" class="${active == 'transaksi' ? 'active' : ''}">↔ &nbsp; Transaksi</a>
            <a href="${ctx}/dompet" class="${active == 'dompet' ? 'active' : ''}">▣ &nbsp; Dompet</a>
            <a href="${ctx}/kategori" class="${active == 'kategori' ? 'active' : ''}">▤ &nbsp; Kategori</a>
            <a href="${ctx}/anggaran" class="${active == 'anggaran' ? 'active' : ''}">◷ &nbsp; Anggaran</a>
            <a href="${ctx}/tabungan" class="${active == 'tabungan' ? 'active' : ''}">★ &nbsp; Target Tabungan</a>
            <a href="${ctx}/hutang" class="${active == 'hutang' ? 'active' : ''}">⇄ &nbsp; Hutang / Piutang</a>
            <a href="${ctx}/berulang" class="${active == 'berulang' ? 'active' : ''}">↻ &nbsp; Transaksi Berulang</a>
            <a href="${ctx}/laporan" class="${active == 'laporan' ? 'active' : ''}">▥ &nbsp; Laporan</a>
            <div class="menu-title">AKUN</div>
            <a href="${ctx}/profil" class="${active == 'profil' ? 'active' : ''}">⚙ &nbsp; Profil</a>
            <c:if test="${sessionScope.user.admin}">
                <a href="${ctx}/admin/users" class="${active == 'admin' ? 'active' : ''}">♛ &nbsp; Kelola Pengguna</a>
            </c:if>
        </nav>
    </aside>

    <main class="main-content">
        <header class="header">
            <div>
                <h1><c:out value="${heading}"/></h1>
                <c:if test="${not empty description}"><p><c:out value="${description}"/></p></c:if>
            </div>
            <div class="profile" style="display:flex;align-items:center;gap:12px;">
                <div class="profile-icon"><c:out value="${sessionScope.user.inisial}"/></div>
                <div>
                    <strong><c:out value="${sessionScope.user.nama}"/></strong>
                    <p><c:out value="${sessionScope.user.email}"/></p>
                    <form action="${ctx}/logout" method="POST" style="margin-top:6px">
                        <input type="hidden" name="_csrf" value="${csrfToken}">
                        <button type="submit" style="cursor:pointer">Logout</button>
                    </form>
                </div>
            </div>
        </header>

        <c:if test="${not empty flashOk}"><div class="alert alert-success"><c:out value="${flashOk}"/></div></c:if>
        <c:if test="${not empty flashErr}"><div class="alert alert-error"><c:out value="${flashErr}"/></div></c:if>
        <c:if test="${not empty errors}">
            <div class="alert alert-error">
                <ul><c:forEach items="${errors}" var="e"><li><c:out value="${e.value}"/></li></c:forEach></ul>
            </div>
        </c:if>

        <jsp:doBody/>
    </main>
</div>
</body>
</html>
