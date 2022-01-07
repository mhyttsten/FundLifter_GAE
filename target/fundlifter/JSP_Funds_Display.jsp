<%@ page import="com.pf.fl.appengine.extract.GCSWrapper" %>
<%@ page import="com.pf.fl.shared.datamodel.DB_FundInfo" %>
<%@ page import="com.pf.fl.appengine.jsphelper.JSP_Helper" %>
<%@ page import="com.pf.fl.appengine.jsphelper.JSP_Constants" %>
<%@ page import="com.pf.fl.shared.datamodel.D_FundInfo" %>

<html>

<head><title>Main</title></head>
<body>
  <h3>All the Funds</h3>

<%
    JSP_Helper.initialize();
    String s = JSP_Helper.fundsDisplayAll();
%>

<%= s %>

</body>
</html>
