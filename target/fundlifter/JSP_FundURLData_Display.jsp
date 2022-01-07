<%@ page import="java.util.logging.Logger" %>
<%@ page import="com.pf.fl.appengine.jsphelper.JSP_Helper_fundURLData" %>
<%@ page import="com.pf.fl.appengine.jsphelper.JSP_Constants" %>
<%@ page import="com.pf.fl.appengine.jsphelper.JSP_Helper" %>
<%@ page import="com.pf.fl.shared.utils.MM" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="com.pf.fl.appengine.extract.GCSWrapper" %>
<%@ page import="com.pf.fl.shared.datamodel.DB_FundInfo" %>
<%@ page import="com.pf.fl.shared.datamodel.D_FundInfo" %>

<!-- PARAM_TYPE is null, INVALID, or fund Type -->

<html>

<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Funds in an Index</title>
<link rel="stylesheet" type="text/css" href="CSSMain.css">
</head>
<body>

<%
    final Logger log = Logger.getLogger("JSP_FundURLData_Display");
    final String TAG = MM.getClassName("JSP_FundURLData_Display");

    response.setContentType("text/html");
    response.setCharacterEncoding("UTF-8");
    JSP_Helper.initialize();

    String argOperation = request.getParameter(JSP_Constants.ARG_OPERATION);

    String typeStr = request.getParameter(JSP_Constants.PARAM_TYPE);
    if (typeStr == null || typeStr.trim().length()== 0) {
       typeStr = null;
    }
    log.info("typeStr is: " + typeStr);

    List<D_FundInfo> list = DB_FundInfo.getFundInfosByType(typeStr);

// COMMIT
if (argOperation != null && argOperation.equals(JSP_Constants.OP_COMMIT)) {
    JSP_Helper_fundURLData.urlControl_VerifyOrExecute(false, request);
    response.sendRedirect("JSP_FundURLData_Display.jsp");
}

// VERIFY
if (argOperation != null && argOperation.equals(JSP_Constants.OP_VERIFY)) { %>
<% String tableData = JSP_Helper_fundURLData.urlControl_VerifyOrExecute(true, request);
   if (tableData == null || tableData.trim().length() == 0) { %>
      <h1>No operations to perform</h1>
<% } else { %>
<form action="JSP_FundURLData_Display.jsp" method="POST" accept-charset="utf-8">
<input type="hidden" name="<%=JSP_Constants.ARG_OPERATION%>" value="<%=JSP_Constants.OP_COMMIT%>">
<div class="selectList3">
<table width="1400">
<%= tableData %>
</table>
</div>
<input type="submit" value="Perform Updates">
</form>
<% } }

// DISPLAY
else { %>
<form action="JSP_FundURLData_Display.jsp" method="POST" accept-charset="utf-8">
<input type="hidden" name="<%=JSP_Constants.ARG_OPERATION%>" value="<%=JSP_Constants.OP_VERIFY%>">
<div class="selectList3">
<table width="1400">
<%= JSP_Helper_fundURLData.urlControl_DisplayFundEmptiesAndList(list) %>
</table>
</div>
<input type="submit" value="Update">
</form>
<% } %>

</body>
</html>
