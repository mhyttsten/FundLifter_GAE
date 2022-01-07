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
    final Logger log = Logger.getLogger("JSP_Report02_Weekly_Display");
    final String TAG = MM.getClassName("JSP_Report02_Weekly_Display");
    JSP_Helper.initialize();

    String s = JSP_Helper.fundReport_WeeklyTable(request, response);
%>
<%=s%>

</body>
</html>

