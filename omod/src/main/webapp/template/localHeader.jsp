<spring:htmlEscape defaultHtmlEscape="true" />
<ul id="menu">
	<li class="first"><a
		href="${pageContext.request.contextPath}/admin"><spring:message
				code="admin.title.short" /></a></li>

	<li
		<c:if test='<%= request.getRequestURI().contains("/export") %>'>class="active"</c:if>>
		<a
		href="${pageContext.request.contextPath}/module/dataexchange/export.form"><spring:message
				code="dataexchange.export" /></a>
	</li>
	<li
		<c:if test='<%= request.getRequestURI().contains("/import") %>'>class="active"</c:if>>
		<a
		href="${pageContext.request.contextPath}/module/dataexchange/import.form"><spring:message
				code="dataexchange.import" /></a>
	</li>
	
	<!-- Add further links here -->
</ul>
<h2>
	<spring:message code="dataexchange.title" />
</h2>
