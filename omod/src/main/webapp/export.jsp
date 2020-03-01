<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>

<%@ include file="template/localHeader.jsp"%>

<openmrs:require privilege="Manage Concepts" otherwise="/login.htm" />

<form method="POST">
Concept IDs to export: <input type="text" name="conceptIds" /> (put whitespace separated ids or leave empty to export all)

<input type="submit"/>
</form>
<p>- OR -</p>
<form action="exportPackageContent.form" method="POST" enctype="multipart/form-data">
Concepts from Metadata Sharing Package Header: <input type="file" name="file"/>
<input type="submit"/>
</form>

<%@ include file="/WEB-INF/template/footer.jsp"%>
