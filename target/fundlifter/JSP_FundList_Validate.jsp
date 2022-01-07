<%@ page import="com.pf.fl.appengine.extract.GCSWrapper" %>
<%@ page import="com.pf.fl.shared.datamodel.DB_FundInfo" %>
<%@ page import="com.pf.fl.appengine.jsphelper.JSP_Helper" %>
<%@ page import="com.pf.fl.appengine.jsphelper.JSP_Constants" %>
<%@ page import="com.pf.fl.shared.datamodel.D_FundInfo" %>

<%
    String type = request.getParameter(JSP_Constants.PARAM_ID);
%>

<html>

<head><title>Validate Funds</title></head>
<body>
<%=JSP_Helper.validateFunds(type)%>
<%=type%><br>
</body>
</html>
