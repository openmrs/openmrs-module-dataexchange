<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>

<%@ include file="template/localHeader.jsp"%>

<openmrs:require privilege="Manage Concepts" otherwise="/login.htm" />

<c:if test="${success}">
<p><b>Import succeeded!</b></p>
</c:if>

<form method="POST" enctype="multipart/form-data">
File to import: <input type="file" name="file" /><input type="submit"/>
</form>

<%@ include file="/WEB-INF/template/footer.jsp"%>