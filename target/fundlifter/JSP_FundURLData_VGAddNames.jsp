<%@ page import="java.util.logging.Logger" %>
<%@ page import="com.pf.fl.appengine.jsphelper.JSP_Constants" %>
<%@ page import="com.pf.fl.appengine.jsphelper.JSP_Helper" %>
<%@ page import="com.pf.fl.appengine.extract.GCSWrapper" %>
<%@ page import="com.pf.fl.shared.datamodel.DB_FundInfo" %>
<%@ page import="com.pf.fl.shared.datamodel.D_FundInfo" %>
<%@ page import="com.pf.fl.shared.utils.MM" %>
<%@ page import="com.pf.fl.shared.Constants" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.ArrayList" %>

<!-- PARAM_TYPE is null, INVALID, or fund Type -->

<html>

<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Funds in an Index</title>
<link rel="stylesheet" type="text/css" href="CSSMain.css">
</head>
<body>

This code has been retired
<%
/*    final Logger log = Logger.getLogger("JSP_FundURLData_Display");
    final String TAG = MM.getClassName("JSP_FundURLData_Display");

    response.setContentType("text/html");
    response.setCharacterEncoding("UTF-8");

    JSP_Helper.initialize();

    String argOperation = request.getParameter(JSP_Constants.ARG_OPERATION);

    List<D_FundInfo> list = DB_FundInfo.getFundInfosByType(D_FundInfo.TYPE_VANGUARD);
    Map<String, Void> hm = new HashMap<>();
    StringBuffer strb = new StringBuffer();
    for (D_FundInfo fi: list) {
       hm.put(fi._nameMS, null);
       strb.append(fi._nameMS + "<br>");
    }

// COMMIT
if (argOperation != null && argOperation.equals(JSP_Constants.OP_COMMIT)) {
String fundsWLF = request.getParameter(JSP_Constants.PARAM_NAME);
String[] funds = fundsWLF.split("\n");

List<String> fundsSaved = new ArrayList<>();
List<String> fundsDups = new ArrayList<>();

boolean didit = false;
for (String s: funds) {
    s = s.trim();
    if (hm.containsKey(s)) {
       fundsDups.add(s);
       continue;
    }

    String url = JSP_Constants.url_getVanguard(s);
    D_FundInfo fi = new D_FundInfo();
    fi._type = D_FundInfo.TYPE_VANGUARD;
    fi._nameOrig = s;
    fi._url = url;
    fi._msRating = -1;
    fi._ppmNumber = "";

    String today = MM.getNowAs_YYMMDD(Constants.TIMEZONE_STOCKHOLM);
    String lfriday = MM.tgif_getLastFridayTodayExcl(today);

    fi._dateYYMMDD_Updated = lfriday;
    fi._dateYYMMDD_Update_Attempted = lfriday;
    JSP_Helper.writeFundInfo(DB_FundInfo.addFundInfo(fi));

    hm.put(s, null);
    fundsSaved.add(s);
}

response.getWriter().print("<h2>Funds added</h2><br>");
for (String s: fundsSaved) {
   response.getWriter().print(s + "<br>");
}

response.getWriter().print("<h2>Funds that already existed</h2><br>");
for (String s: fundsDups) {
   response.getWriter().print(s + "<br>");
}

// response.getWriter().print("<h2>Already existed dump</h2><br>");
// response.getWriter().print(strb.toString());
}
else {
%>

<!--
<form action="JSP_FundURLData_VGAddNames.jsp" method="POST" accept-charset="utf-8" id="usrform">
<input type="hidden" name="<%=JSP_Constants.ARG_OPERATION%>" value="<%=JSP_Constants.OP_COMMIT%>">
<br>
<input type="submit" value="Add Vanguard Funds">
</form>
<textarea rows="50" cols="80" name="<%=JSP_Constants.PARAM_NAME%>" form="usrform"></textarea>
-->

<% } */ %>

</body>
</html>

