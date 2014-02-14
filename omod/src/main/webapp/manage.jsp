<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>

<%@ include file="template/localHeader.jsp"%>

<p>Hello ${user.systemId}!</p>

<form method="POST">
Concept IDs to export: <input type="text" name="conceptIds" /><input type="submit"/>
</form>

<%@ include file="/WEB-INF/template/footer.jsp"%>