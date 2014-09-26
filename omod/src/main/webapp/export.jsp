<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>

<%@ include file="template/localHeader.jsp"%>

<form method="POST">
Concept IDs to export: <input type="text" name="conceptIds" /> (put whitespace separated ids or leave empty to export all)

<input type="submit"/>
</form>

<%@ include file="/WEB-INF/template/footer.jsp"%>