<%@ page import="com.pf.fl.appengine.jsphelper.JSP_Helper" %>
<%@ page import="com.pf.fl.shared.utils.MM" %>
<%@ page import="java.util.logging.Logger" %>

<!-- PARAM_TYPE is null, INVALID, or fund Type -->

<html>

<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Funds in an Index</title>
<link rel="stylesheet" type="text/css" href="CSSMain.css">
</head>
<body>

<%
    JSP_Helper.initialize();
    String s = JSP_Helper.jsp_extractDebugger(request, response);
    s = s.replace("\n", "<br>");
%>
<%=s%>
Done HTML
</body>
</html>

