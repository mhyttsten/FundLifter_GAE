<%@ page import="com.pf.fl.appengine.jsphelper.JSP_Helper" %>
<%@ page import="java.util.List" %>
<%@ page import="java.net.URLDecoder" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="com.pf.fl.appengine.jsphelper.JSP_Constants" %>
<%@ page import="com.pf.fl.shared.datamodel.D_FundInfo" %>
<%@ page import="com.pf.fl.appengine.extract.GCSWrapper" %>
<%@ page import="com.pf.fl.shared.datamodel.DB_FundInfo" %>

<html>

<head><title>Funds in an Index</title></head>
<body>
  <%
    JSP_Helper.initialize();
    String id = request.getParameter(JSP_Constants.ARG_ID);
    id = URLDecoder.decode(id, "UTF-8");
    List<D_FundInfo> l = DB_FundInfo.getFundInfosByIndex(id);
  %>
  <h1>Funds for Index: <%=id%> (<%=l.size()%>)</h1>
  <%
    for (int i=0; i < l.size(); i++) {
        D_FundInfo fundInfo = l.get(i);
  %>
  <%=fundInfo.getTypeAndName()%><br>
  <% } %>

</body>
</html>

